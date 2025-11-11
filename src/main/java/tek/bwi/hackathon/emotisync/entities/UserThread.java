package tek.bwi.hackathon.emotisync.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@NoArgsConstructor
@Document(collection = "threads")
public class UserThread {
    @Id
    private String threadId;
    private String requestId;
    private String participantId;
    private String threadType;
}
