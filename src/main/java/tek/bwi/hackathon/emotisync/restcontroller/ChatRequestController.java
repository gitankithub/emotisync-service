package tek.bwi.hackathon.emotisync.restcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.ChatMessage;
import tek.bwi.hackathon.emotisync.entities.ChatRequest;
import tek.bwi.hackathon.emotisync.models.BestMatchScore;
import tek.bwi.hackathon.emotisync.models.ChatResponse;
import tek.bwi.hackathon.emotisync.service.ChatRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chatQueries")
public class ChatRequestController {

    private final ChatRequestService chatRequestService;

    public ChatRequestController(ChatRequestService chatRequestService) {
        this.chatRequestService = chatRequestService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendChatMessage(
            @RequestBody ChatMessage message
    ) {
        log.info("Received chat message: {}", message);
        try {
            return ResponseEntity.ok(chatRequestService.handleChatQuery(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String threadId, @RequestParam(required = false) String status){
        List<ChatMessage> history = chatRequestService.getChatHistory(threadId, status);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/checkExistingRequest")
    public ResponseEntity<BestMatchScore> checkExistingServiceRequest(@RequestBody ChatMessage chatMessage){
        return ResponseEntity.ok(chatRequestService.checkExistingServiceRequest(chatMessage));
    }

    @GetMapping("/close")
    public ResponseEntity<ChatRequest> closeSession(@RequestParam String chatRequestId){
        chatRequestService.closeChatRequest(chatRequestId);
        return ResponseEntity.ok(chatRequestService.closeChatRequest(chatRequestId));
    }
}
