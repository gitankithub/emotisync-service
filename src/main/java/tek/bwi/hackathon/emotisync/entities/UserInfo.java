package tek.bwi.hackathon.emotisync.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tek.bwi.hackathon.emotisync.models.UserRole;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class UserInfo {
    @Id
    private String userId;
    private String email;
    private String name;
    private UserRole role;
}

