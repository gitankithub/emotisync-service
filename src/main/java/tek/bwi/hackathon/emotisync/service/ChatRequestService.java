package tek.bwi.hackathon.emotisync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.client.GeminiEmbeddingClient;
import tek.bwi.hackathon.emotisync.entities.*;
import tek.bwi.hackathon.emotisync.entities.ChatMessage;
import tek.bwi.hackathon.emotisync.models.*;
import tek.bwi.hackathon.emotisync.models.gemini.ChatServiceLLMResponse;
import tek.bwi.hackathon.emotisync.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatRequestService {

    private final ObjectMapper objectMapper;
    private final RequestRepository requestRepo;
    private final MessageRepository messageRepo;
    private final ChatRequestRepository chatRequestRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReservationRepository reservationRepository;
    private final GeminiEmbeddingClient embeddingClient;
    private final GeminiClient geminiClient;
    private final UserService userService;
    private final PromptBuilderService promptBuilderService;

    public ChatRequestService(ObjectMapper objectMapper, RequestRepository requestRepo,
                              MessageRepository messageRepo,
                              ChatRequestRepository chatRequestRepository,
                              ChatMessageRepository chatMessageRepository,
                              ReservationRepository reservationRepository, GeminiEmbeddingClient embeddingClient,
                              GeminiClient geminiClient, UserService userService, PromptBuilderService promptBuilderService) {
        this.objectMapper = objectMapper;
        this.requestRepo = requestRepo;
        this.messageRepo = messageRepo;
        this.chatRequestRepository = chatRequestRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.reservationRepository = reservationRepository;
        this.embeddingClient = embeddingClient;
        this.geminiClient = geminiClient;
        this.userService = userService;
        this.promptBuilderService = promptBuilderService;
    }

    private double cosineSimilarity(List<Float> v1, List<Float> v2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    // Create new session for new chat or return existing session for reply
    public ChatRequest ensureChatRequestSession(String chatRequestId, String senderId) {
        if (chatRequestId == null || chatRequestId.isEmpty()) {
            ChatRequest chatRequest = new ChatRequest();
            chatRequest.setId(UUID.randomUUID().toString());
            chatRequest.setSenderId(senderId);
            chatRequest.setCreatedAt(Instant.now());
            chatRequest.setUpdatedAt(Instant.now());
            chatRequest.setStatus("ACTIVE");
            return chatRequest;
        } else {
            return chatRequestRepository.findById(chatRequestId).orElseThrow(() -> new RuntimeException("failed to retrieve chat request."));
        }
    }

    // Handles chat, matching, LLM, staff messages
    public ChatResponse handleChatQuery(ChatMessage message) throws JsonProcessingException {
        ChatRequest chatRequest = ensureChatRequestSession(message.getChatRequestId(), message.getSenderId());
        log.info("Handling chat query for session {}: {}", chatRequest.getId(), message);
        // Find ServiceRequest match
        BestMatchScore bestMatchScore = findBestMatchScore(message);
        log.info("Best match score: {}", bestMatchScore);
        UserInfo userInfo = userService.getById(message.getSenderId());
        Reservation reservation = reservationRepository.findByGuestIdAndStatus(message.getSenderId(), "CHECKED_IN");
        List<ChatMessage> chatHistory = null;
        if (message.getChatRequestId() != null) {
            chatHistory = chatMessageRepository.findByChatRequestIdAndStatusOrderByTimestampAsc(message.getChatRequestId(), "ACTIVE");
        }
        String prompt = promptBuilderService.buildChatQueryPrompt(message.getMessage(), bestMatchScore.getBestMatch(), bestMatchScore.getBestScore(), reservation, userInfo, chatHistory);
        log.info("Constructed prompt for LLM: {}", prompt);
        LLMRequest geminiRequest = new LLMRequest(List.of(
                new LLMPayload(List.of(new PayloadPart(prompt)))
        ));
        String payload = objectMapper.writeValueAsString(geminiRequest);
        ChatServiceLLMResponse llmResponse = geminiClient.sendPrompt(payload, ChatServiceLLMResponse.class);
        log.info("LLM Response: {}", llmResponse);

        if (bestMatchScore.getBestMatch() != null && !"NORMAL".equalsIgnoreCase(llmResponse.getAction())) {
            switch (llmResponse.getAction().toUpperCase()) {
                case "ESCALATE", "DELAY":
                    bestMatchScore.getBestMatch().setStatus(ServiceRequestStatus.ESCALATED);
                    requestRepo.save(bestMatchScore.getBestMatch());
                    break;
                case "COMPLETE":
                case "CLOSED":
                    bestMatchScore.getBestMatch().setStatus(ServiceRequestStatus.fromCode(llmResponse.getAction().toUpperCase()));
                    requestRepo.save(bestMatchScore.getBestMatch());
                    break;
                default: break;
            }
            addAllResponseMessages(llmResponse, bestMatchScore.getBestMatch());
        }

        ChatMessage guestMsg = new ChatMessage();
        guestMsg.setChatRequestId(chatRequest.getId());
        guestMsg.setCreatedBy(UserRole.GUEST);
        guestMsg.setMessage(message.getMessage());
        guestMsg.setTimestamp(Instant.now());
        chatRequestRepository.save(chatRequest);
        chatMessageRepository.save(guestMsg);

        // Assistant response to guest in chat
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setChatRequestId(chatRequest.getId());
        assistantMsg.setCreatedBy(UserRole.ASSISTANT);
        assistantMsg.setMessage(llmResponse.getReplyToGuest());
        assistantMsg.setTimestamp(Instant.now());
        chatMessageRepository.save(assistantMsg);

        if (llmResponse.isShouldClose()) {
            chatRequest = closeChatRequest(chatRequest.getId());
            log.info("Chat session closed by LLM, reason: " + llmResponse.getReason());
        }
        return new ChatResponse(chatRequest, llmResponse.getReplyToGuest());
    }

    private BestMatchScore findBestMatchScore(ChatMessage message) {
        List<ServiceRequest> activeRequests = requestRepo.findByGuestIdAndStatusIn(
                message.getSenderId(), List.of("OPEN", "ASSIGNED", "IN_PROGRESS", "ESCALATED"));
        log.info("Found {} active requests for guest {}", activeRequests.size(), message.getSenderId());
        List<Float> queryEmbedding = embeddingClient.embedText(message.getMessage());
        ServiceRequest bestMatch = null;
        double bestScore = 0;
        for (ServiceRequest sr : activeRequests) {
            List<Float> srEmbedding = embeddingClient.embedText(sr.getRequestTitle());
            double score = cosineSimilarity(queryEmbedding, srEmbedding);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = sr;
            }
        }
        return new BestMatchScore(bestScore, bestMatch);
    }

    public List<ChatMessage> getChatHistory(String chatRequestId, String status) {
        return chatMessageRepository.findByChatRequestIdAndStatusOrderByTimestampAsc(chatRequestId, status);
    }

    public ChatRequest closeChatRequest(String chatRequestId) {
        ChatRequest chatRequest = chatRequestRepository.findById(chatRequestId).orElse(null);
        if (chatRequest != null) {
            chatRequest.setUpdatedAt(Instant.now());
            chatRequest.setStatus("CLOSED");
            chatRequestRepository.save(chatRequest);
        }
        return chatRequest;
    }

    private void addAllResponseMessages(
            ChatServiceLLMResponse llmResponse,
            ServiceRequest request) {
        if (nonEmpty(llmResponse.getReplyToGuest())) {
            Message guestMsg = new Message();
            guestMsg.setUserId(request.getGuestId());
            guestMsg.setThreadId(request.getUserThread().getThreadId());
            guestMsg.setContent(llmResponse.getReplyToGuest());
            guestMsg.setCreatedBy(UserRole.ASSISTANT);
            guestMsg.setVisibility(List.of(UserRole.GUEST, UserRole.ADMIN));
            guestMsg.setTime(Instant.now().toString());
            messageRepo.save(guestMsg);
        }
        if (nonEmpty(llmResponse.getReplyToStaff())) {
            Message staffMsg = new Message();
            staffMsg.setUserId(request.getAssignedTo());
            staffMsg.setThreadId(request.getUserThread().getThreadId());
            staffMsg.setContent(llmResponse.getReplyToStaff());
            staffMsg.setCreatedBy(UserRole.ASSISTANT);
            staffMsg.setVisibility(List.of(UserRole.STAFF, UserRole.ADMIN));
            staffMsg.setTime(Instant.now().toString());
            messageRepo.save(staffMsg);
        }
        if (nonEmpty(llmResponse.getReplyToAdmin())) {
            Message adminMsg = new Message();
            request.getUserThread().getParticipantIds().stream()
                    .filter(participant -> "ADMIN".equalsIgnoreCase(participant.getRole()))
                    .findFirst()
                    .ifPresent(participant -> adminMsg.setUserId(participant.getId()));
            adminMsg.setThreadId(request.getUserThread().getThreadId());
            adminMsg.setContent(llmResponse.getReplyToAdmin());
            adminMsg.setCreatedBy(UserRole.ASSISTANT);
            adminMsg.setVisibility(List.of(UserRole.ADMIN));
            adminMsg.setTime(Instant.now().toString());
            messageRepo.save(adminMsg);
        }
    }

    private boolean nonEmpty(String val) {
        return val != null && !val.trim().isEmpty();
    }

    public BestMatchScore checkExistingServiceRequest(ChatMessage chatMessage) {
        BestMatchScore bestMatchScore = findBestMatchScore(chatMessage);
        if (bestMatchScore.getBestScore() > 0.7) {
            return bestMatchScore;
        } else {
            return null;
        }
    }
}
