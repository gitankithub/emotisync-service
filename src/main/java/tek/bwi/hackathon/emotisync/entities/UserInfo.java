package tek.bwi.hackathon.emotisync.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class UserInfo {
    @Id
    private String userId;
    private String name;
    private String role; // GUEST, STAFF, ADMIN
}

