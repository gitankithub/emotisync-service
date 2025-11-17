package tek.bwi.hackathon.emotisync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.client.GeminiEmbeddingClient;
import tek.bwi.hackathon.emotisync.entities.ChatMessage;
import tek.bwi.hackathon.emotisync.entities.ChatRequest;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.models.ChatResponse;
import tek.bwi.hackathon.emotisync.models.ServiceRequestStatus;
import tek.bwi.hackathon.emotisync.models.UserRole;
import tek.bwi.hackathon.emotisync.models.gemini.ChatServiceLLMResponse;
import tek.bwi.hackathon.emotisync.repository.ChatMessageRepository;
import tek.bwi.hackathon.emotisync.repository.ChatRequestRepository;
import tek.bwi.hackathon.emotisync.repository.MessageRepository;
import tek.bwi.hackathon.emotisync.repository.RequestRepository;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class ChatRequestService {

    private final RequestRepository requestRepo;
    private final MessageRepository messageRepo;
    private final ChatRequestRepository chatRequestRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiEmbeddingClient embeddingClient;
    private final GeminiClient geminiClient;
    
    public ChatRequestService(RequestRepository requestRepo,
                                MessageRepository messageRepo,
                                ChatRequestRepository chatRequestRepository,
                                ChatMessageRepository chatMessageRepository,
                                GeminiEmbeddingClient embeddingClient,
                                GeminiClient geminiClient) {
        this.requestRepo = requestRepo;
        this.messageRepo = messageRepo;
        this.chatRequestRepository = chatRequestRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.embeddingClient = embeddingClient;
        this.geminiClient = geminiClient;
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
    public ChatRequest ensureChatRequestSession(String chatSessionId, String guestId) {
        if (chatSessionId == null) {
            ChatRequest session = new ChatRequest();
            session.setGuestId(guestId);
            session.setCreatedAt(Instant.now());
            session.setUpdatedAt(Instant.now());
            session.setStatus("ACTIVE");
            return chatRequestRepository.save(session);
        } else {
            return chatRequestRepository.findById(chatSessionId).orElseThrow();
        }
    }

    // Handles chat, matching, LLM, staff messages
    public ChatResponse handleChatQuery(String adhocSessionId, String guestId, String chatQuery) {
        ChatRequest chatRequest = ensureChatRequestSession(adhocSessionId, guestId);

        ChatMessage guestMsg = new ChatMessage();
        guestMsg.setChatRequestId(chatRequest.getId());
        guestMsg.setSenderRole(UserRole.GUEST);
        guestMsg.setVisibility(List.of(UserRole.GUEST));
        guestMsg.setMessage(chatQuery);
        guestMsg.setTimestamp(Instant.now());
        chatMessageRepository.save(guestMsg);

        // Find ServiceRequest match
        List<ServiceRequest> activeRequests = requestRepo.findByGuestIdAndStatus(
                guestId, List.of("OPEN", "ASSIGNED", "IN_PROGRESS", "ESCALATED"));
        List<Float> queryEmbedding = embeddingClient.embedText(chatQuery);

        ServiceRequest bestMatch = null;
        double bestScore = 0;
        for (ServiceRequest sr : activeRequests) {
            String embText = sr.getRequestTitle() + " " + (sr.getRequestDescription() != null ? sr.getRequestDescription() : "");
            List<Float> srEmbedding = embeddingClient.embedText(embText);
            double score = cosineSimilarity(queryEmbedding, srEmbedding);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = sr;
            }
        }
        String prompt = getString(chatQuery, bestMatch, bestScore);
        ChatServiceLLMResponse llmResponse = geminiClient.sendPrompt(prompt, ChatServiceLLMResponse.class);
        log.info("LLM Response: {}", llmResponse);

        if (bestMatch != null && !"NORMAL".equalsIgnoreCase(llmResponse.getAction())) {
            switch (llmResponse.getAction().toUpperCase()) {
                case "ESCALATE":
                    bestMatch.setStatus(ServiceRequestStatus.ESCALATED);
                    requestRepo.save(bestMatch);
                    saveServiceRequestStaffMsg(bestMatch, "Guest inquiry escalated. Please respond.");
                    break;
                case "COMPLETE":
                case "CLOSED":
                    bestMatch.setStatus(ServiceRequestStatus.COMPLETED);
                    requestRepo.save(bestMatch);
                    saveServiceRequestStaffMsg(bestMatch, "Request marked complete by guest inquiry.");
                    break;
                case "DELAY":
                    bestMatch.setStatus(ServiceRequestStatus.ESCALATED);
                    requestRepo.save(bestMatch);
                    saveServiceRequestStaffMsg(bestMatch, "Guest reported delay. Staff notified.");
                    break;
                default: break;
            }
        }

        // Assistant response to guest in chat
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setChatRequestId(chatRequest.getId());
        assistantMsg.setSenderRole(UserRole.ASSISTANT);
        assistantMsg.setVisibility(List.of(UserRole.ASSISTANT));
        assistantMsg.setMessage(llmResponse.getReply());
        assistantMsg.setTimestamp(Instant.now());
        chatMessageRepository.save(assistantMsg);

        if (llmResponse.isShouldClose()) {
            chatRequest = closeChatRequest(chatRequest.getId());
            log.info("Chat session closed by LLM, reason: " + llmResponse.getReason());
        }
        return new ChatResponse(chatRequest, llmResponse.getReply());
    }

    private static String getString(String chatQuery, ServiceRequest bestMatch, double bestScore) {
        String prompt;
        if (bestMatch != null && bestScore > 0.8) {
            prompt = "Guest asked: \"" + chatQuery + "\"\n"
                    + "Related request: Title='" + bestMatch.getRequestTitle()
                    + "', Status='" + bestMatch.getStatus()
                    + "', Description='" + bestMatch.getRequestDescription() + "'\n"
                    + "Decide next action: escalate, complete, normal reply, AND decide if the chat session should be closed."
                    + "Respond JSON: { \"action\": \"...\", \"reply\": \"...\", \"shouldClose\": true/false, \"reason\": \"...\" }";
        } else {
            prompt = "Guest asked: \"" + chatQuery + "\"\n"
                    + "No related open requests. Respond with help or new request option as JSON: { \"action\": \"...\", \"reply\": \"...\" }";
        }
        return prompt;
    }

    private void saveServiceRequestStaffMsg(ServiceRequest serviceRequest, String text) {
        Message msg = new Message();
        msg.setThreadId(serviceRequest.getUserThread().getThreadId());
        msg.setCreatedBy(UserRole.ASSISTANT);
        msg.setVisibility(List.of(UserRole.STAFF, UserRole.ADMIN));
        msg.setContent(text);
        msg.setTime(Instant.now().toString());
        messageRepo.save(msg);
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
}
