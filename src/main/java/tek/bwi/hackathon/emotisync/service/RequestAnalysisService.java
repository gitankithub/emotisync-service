package tek.bwi.hackathon.emotisync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.entities.GuestFeedback;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.models.*;
import tek.bwi.hackathon.emotisync.repository.MessageRepository;
import tek.bwi.hackathon.emotisync.repository.RequestAnalysisRepository;
import tek.bwi.hackathon.emotisync.repository.RequestRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RequestAnalysisService {
    private final RequestRepository requestRepo;
    private final ObjectMapper objectMapper;
    private final RequestAnalysisRepository requestAnalysisRepo;
    private final GeminiClient geminiClient;
    private final PromptBuilderService promptBuilderService;
    private final MessageRepository messageRepository;

    public RequestAnalysisService(RequestRepository requestRepo, ObjectMapper objectMapper, RequestAnalysisRepository requestAnalysisRepo, GeminiClient geminiClient, PromptBuilderService promptBuilderService, MessageRepository messageRepository) {
        this.requestRepo = requestRepo;
        this.objectMapper = objectMapper;
        this.requestAnalysisRepo = requestAnalysisRepo;
        this.geminiClient = geminiClient;
        this.promptBuilderService = promptBuilderService;
        this.messageRepository = messageRepository;
    }

    public RequestAnalysis getOrRunAnalysis(String requestId) throws JsonProcessingException {
        // 1. Check cache (MongoDB) for existing analysis
        Optional<RequestAnalysis> cached = requestAnalysisRepo.findByRequestId(requestId);
        if (cached.isPresent()) {
            log.info("Returning cached analysis for requestId: {}", requestId);
            return cached.get();
        }

        // 2. Not in cache, so generate new analysis
        ServiceRequest req = requestRepo.findByRequestIdAndStatusIn(requestId, List.of("COMPLETED", "CLOSED"))
                .orElseThrow(() -> new RuntimeException("ServiceRequest not found: " + requestId));
        log.info("Generating analysis for ServiceRequest: {}", req);
        List<Message> messages = messageRepository
                .findByThreadIdOrderByTimeAsc(req.getUserThread().getThreadId());
        log.info("Fetched {} messages for threadId {}", messages.size(), req.getUserThread().getThreadId());
        // Extract chat/messages, feedback, SLA metrics from "req"
        List<String> emojiFeedback = extractEmojiFeedback(messages);
        String writtenFeedback = extractWrittenFeedback(messages);
        int responseDelayMinutes = computeResponseDelayMinutes(messages);
        boolean slaBreached = computeSlaBreached(messages);

        String prompt = promptBuilderService.buildRequestAnalysisPrompt(req, messages, emojiFeedback, writtenFeedback, responseDelayMinutes, slaBreached);
        log.info("Constructed Prompt: {}", prompt);
        LLMRequest geminiRequest = new LLMRequest(List.of(
                new LLMPayload(List.of(new PayloadPart(prompt)))
        ));
        String payload = objectMapper.writeValueAsString(geminiRequest);
        RequestAnalysis llmResponse = geminiClient.sendPrompt(payload, RequestAnalysis.class);
        // Set the service requestId (if not already set by GeminiClient)
        llmResponse.setRequestId(requestId);
        // 3. Save analysis to MongoDB for caching
        requestAnalysisRepo.save(llmResponse);
        // 4. Return analysis
        return llmResponse;
    }

    private List<Message> getSafeMessages(ServiceRequest req) {
        if (req.getUserThread() != null && req.getUserThread().getMessages() != null) {
            return req.getUserThread().getMessages();
        }
        return List.of();
    }

    private List<String> extractEmojiFeedback(List<Message> messages) {
        List<String> emojiFeedback = new ArrayList<>();
        for (Message msg : messages) {
            GuestFeedback feedback = msg.getGuestFeedback();
            if (feedback != null && feedback.getRating() != null) {
                emojiFeedback.add(feedback.getRating());
            }
        }
        return emojiFeedback;
    }

    private String extractWrittenFeedback(List<Message> messages) {
        for (Message msg : messages) {
            GuestFeedback feedback = msg.getGuestFeedback();
            if (feedback != null && feedback.getFeedbackText() != null && !feedback.getFeedbackText().isBlank()) {
                return feedback.getFeedbackText();
            }
        }
        return "";
    }

    private int computeResponseDelayMinutes(List<Message> messages) {
        // Example: Compute min/max time difference between first guest and staff message, customize as needed
        String firstGuestTime = null;
        String firstStaffTime = null;
        for (Message msg : messages) {
            if (msg.getCreatedBy() == UserRole.GUEST && firstGuestTime == null) {
                firstGuestTime = msg.getTime();
            }
            if (msg.getCreatedBy() == UserRole.STAFF && firstStaffTime == null) {
                firstStaffTime = msg.getTime();
            }
        }
        if (firstGuestTime != null && firstStaffTime != null) {
            Instant guestT = Instant.parse(firstGuestTime);
            Instant staffT = Instant.parse(firstStaffTime);
            return (int) Duration.between(guestT, staffT).toMinutes();
        }
        return 0;
    }

    private boolean computeSlaBreached(List<Message> messages) {
        // Example: flag SLA breach if response delay > 15 min
        return computeResponseDelayMinutes(messages) > 15;
    }

}