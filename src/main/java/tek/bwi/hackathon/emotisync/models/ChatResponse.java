package tek.bwi.hackathon.emotisync.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tek.bwi.hackathon.emotisync.entities.ChatRequest;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private ChatRequest chatRequest;
    private String assistantReply;
}
