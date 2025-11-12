package tek.bwi.hackathon.emotisync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.models.GeminiContent;
import tek.bwi.hackathon.emotisync.models.GeminiPart;
import tek.bwi.hackathon.emotisync.models.GeminiRequest;
import tek.bwi.hackathon.emotisync.models.LLMResponse;
import tek.bwi.hackathon.emotisync.repository.ReservationRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class LLMService {
    private final ObjectMapper objectMapper;
    private final PromptBuilderService promptBuilder;
    private final GeminiClient geminiClient;
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final LLMOrchestrationService llmOrchestrationService;

    @Autowired
    public LLMService(ObjectMapper objectMapper, PromptBuilderService promptBuilder, GeminiClient geminiClient, UserService userService, ReservationRepository reservationRepository, LLMOrchestrationService llmOrchestrationService) {
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.geminiClient = geminiClient;
        this.userService = userService;
        this.reservationRepository = reservationRepository;
        this.llmOrchestrationService = llmOrchestrationService;
    }

    public void processGuestMessage(Message message, List<Message> chatHistory) {
        try {
            UserInfo userInfo = userService.getById(message.getUserId());
            Reservation reservation = reservationRepository.findByUserIdAndStatus(message.getUserId(), "CHECKED_IN");
            String prompt = promptBuilder.buildGuestPrompt(message, userInfo, reservation, chatHistory);

            GeminiRequest geminiRequest = new GeminiRequest(List.of(
                    new GeminiContent(List.of(new GeminiPart(prompt)))
            ));
            String payload = null;
            payload = objectMapper.writeValueAsString(geminiRequest);
            LLMResponse llmResponse = geminiClient.sendPrompt(payload);
            llmOrchestrationService.handleLLMResponse(llmResponse, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void processAdminStaffMessage(Message message, List<Message> chatHistory) {
        try {
            UserInfo userInfo = userService.getById(message.getUserId());
            Reservation reservation = reservationRepository.findByUserIdAndStatus(message.getUserId(), "CHECKED_IN");
            String prompt = promptBuilder.buildStaffPrompt(
                    message,
                    Objects.requireNonNull(userInfo),
                    getGuestInfo(reservation),
                    reservation,
                    chatHistory
            );

            GeminiRequest geminiRequest = new GeminiRequest(List.of(
                    new GeminiContent(List.of(new GeminiPart(prompt)))
            ));
            String payload = null;
            payload = objectMapper.writeValueAsString(geminiRequest);
            LLMResponse llmResponse = geminiClient.sendPrompt(payload);
            llmOrchestrationService. handleLLMResponse(llmResponse, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserInfo getGuestInfo(Reservation reservation) {
        // Using Optional to handle potential null value of reservation
        return Optional.ofNullable(reservation)
                .map(Reservation::getUserId)
                .map(userService::getById)
                .orElse(null);
    }
}
