package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.ChatMessage;
import tek.bwi.hackathon.emotisync.entities.ChatRequest;
import tek.bwi.hackathon.emotisync.models.ChatResponse;
import tek.bwi.hackathon.emotisync.service.ChatRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/chatQueries")
public class ChatRequestController {

    private final ChatRequestService chatRequestService;

    public ChatRequestController(ChatRequestService chatRequestService) {
        this.chatRequestService = chatRequestService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendChatMessage(
            @RequestParam(required = false) String chatRequestId,
            @RequestParam String guestId,
            @RequestBody String message
    ) {
        ChatRequest session = chatRequestService.ensureChatRequestSession(chatRequestId, guestId);
        return ResponseEntity.ok(chatRequestService.handleChatQuery(session.getId(), guestId, message));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String threadId) {
        List<ChatMessage> history = chatRequestService.getChatHistory(threadId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/close")
    public ResponseEntity<ChatRequest> closeSession(@RequestParam String chatRequestId){
        chatRequestService.closeChatRequest(chatRequestId);
        return ResponseEntity.ok(chatRequestService.closeChatRequest(chatRequestId));
    }
}
