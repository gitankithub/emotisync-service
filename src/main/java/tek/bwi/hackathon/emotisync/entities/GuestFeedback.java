package tek.bwi.hackathon.emotisync.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestFeedback {
    private String feedbackId;
    private String guestId;
    private String feedbackText;
    private String rating;
}
