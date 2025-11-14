package tek.bwi.hackathon.emotisync.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import tek.bwi.hackathon.emotisync.models.ServiceRequestStatus;
import tek.bwi.hackathon.emotisync.models.ThreadParticipant;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "threads")
public class UserThread {
    @Id
    private String threadId;
    private String requestId;
    private List<ThreadParticipant> participantIds;
    private ServiceRequestStatus status;
    @DBRef
    private List<Message> messages;
    private String createdAt;
    private String updatedAt;
}
