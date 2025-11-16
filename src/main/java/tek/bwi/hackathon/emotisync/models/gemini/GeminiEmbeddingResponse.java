package tek.bwi.hackathon.emotisync.models.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiEmbeddingResponse {
    private List<Float> embedding;
}

