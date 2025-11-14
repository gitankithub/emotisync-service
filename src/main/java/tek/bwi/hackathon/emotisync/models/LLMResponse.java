package tek.bwi.hackathon.emotisync.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LLMResponse {
    private String responseForGuest;
    private String responseForStaff;
    private String responseForAdmin;
    private boolean actionNeeded;
    private ActionDetail actionDetail;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActionDetail {
        private ActionDetailEnum type;     // e.g. "escalate", "createServiceRequest", "completed", "closeRequest"
        private String targetUserRole;    // e.g. "staff", "admin"
        private String title;             // e.g. "Escalate Room Cleaning"
        private String description;       // e.g. "Guest has complained twice"
        private String urgency;           // e.g. "urgent"
        private String escalationTarget;  // e.g. "serviceRequest", "thread", "both"
    }
}

