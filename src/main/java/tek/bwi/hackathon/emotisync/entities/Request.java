package tek.bwi.hackathon.emotisync.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "request")
public class Request {
    @Id
    private String id;
    private String threadId;
    private String guestId;
    private String senderId;
    private String text;
    private String messageType;
    private String status;
    private String createdAt;
    private String updatedAt;
}



