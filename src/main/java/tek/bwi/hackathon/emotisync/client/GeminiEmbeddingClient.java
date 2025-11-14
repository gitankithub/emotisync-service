package tek.bwi.hackathon.emotisync.client;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tek.bwi.hackathon.emotisync.models.gemini.EmbeddingContent;
import tek.bwi.hackathon.emotisync.models.gemini.EmbeddingPart;
import tek.bwi.hackathon.emotisync.models.gemini.EmbeddingRequest;
import tek.bwi.hackathon.emotisync.models.gemini.GeminiEmbeddingResponse;

import java.util.List;

@Service
public class GeminiEmbeddingClient {
    private final WebClient webClient;
    private final String endpointPath;
    private final String apiKey;

    public GeminiEmbeddingClient(
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.embedding.endpoint}") String endpointPath
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.endpointPath = endpointPath;
        this.apiKey = apiKey;
    }

    public List<Float> embedText(String text) {
        // Build JSON body as per Gemini embeddings format
        var requestBody = new EmbeddingRequest(
                new EmbeddingContent(List.of(new EmbeddingPart(text)))
        );

        GeminiEmbeddingResponse response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(endpointPath)
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiEmbeddingResponse.class)
                .block();

        if (response != null && response.getEmbedding() != null) {
            return response.getEmbedding();
        } else {
            throw new RuntimeException("Failed to get embedding from Gemini");
        }
    }
}
