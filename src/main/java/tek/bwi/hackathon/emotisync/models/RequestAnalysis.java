package tek.bwi.hackathon.emotisync.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "request_analysis")
public class RequestAnalysis {
    private String requestId;                 // (Optional: for easy reference)
    private String summary;
    private Double guestSentimentScore;       // Number (null if unavailable)
    private String guestAttitude;             // Label (null if unavailable)
    private Double staffSentimentScore;       // Number (null if unavailable)
    private String staffAttitude;             // Label (null if unavailable)
    private Boolean slaBreached;
    private String mainComplaint;
    private String improvementSuggestion;
    private String escalationReason;
    private Integer responseDelayMinutes;
    private List<String> emotionalKeywords;
}
