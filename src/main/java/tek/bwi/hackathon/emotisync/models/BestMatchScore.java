package tek.bwi.hackathon.emotisync.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestMatchScore {
    private Double bestScore;
    private ServiceRequest bestMatch;
}
