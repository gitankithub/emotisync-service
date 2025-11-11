package tek.bwi.hackathon.emotisync.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessage {
    private String sender;
    private String message;
}

