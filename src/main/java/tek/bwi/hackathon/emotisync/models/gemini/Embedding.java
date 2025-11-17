package tek.bwi.hackathon.emotisync.models.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Embedding {
    private List<Float> values;
}
