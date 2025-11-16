package tek.bwi.hackathon.emotisync.models.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {
    private EmbeddingContent content;
}

