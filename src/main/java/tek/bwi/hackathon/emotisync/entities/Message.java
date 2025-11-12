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
    private String messageId;
    private String threadId;
    private String userId;
    private String userRole;
    private String content;
    private GuestFeedback guestFeedback;
    private String time;
}
