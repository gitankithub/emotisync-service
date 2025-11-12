package tek.bwi.hackathon.emotisync.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request")
public class ServiceRequest {
    @Id
    private String requestId;
    private String requestTitle;
    private String requestDescription;
    private String requestUrgency;
    private String assignedTo;
    private String guestId;
    private String status;
    @DBRef
    private UserThread userThread;
    private String createdAt;
    private String updatedAt;
}



