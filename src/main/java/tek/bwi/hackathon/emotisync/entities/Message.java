package tek.bwi.hackathon.emotisync.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String threadId;
    private String senderId;
    private String senderRole;
    private String message;
    private String time;
}
