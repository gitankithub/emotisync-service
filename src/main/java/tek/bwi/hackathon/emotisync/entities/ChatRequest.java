package tek.bwi.hackathon.emotisync.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chatRequests")
public class ChatRequest {
    @Id
    private String id;
    private String senderId;
    private String status; // e.g. ACTIVE, CLOSED
    private Instant createdAt;
    private Instant updatedAt;
}