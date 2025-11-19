package tek.bwi.hackathon.emotisync.models.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatServiceLLMResponse {
    private String action;
    private String replyToGuest;
    private String replyToStaff;
    private String replyToAdmin;
    private boolean shouldClose;
    private String reason;
}
