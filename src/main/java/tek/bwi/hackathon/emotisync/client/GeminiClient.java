package tek.bwi.hackathon.emotisync.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.models.LLMResponse;

import java.time.Duration;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String endpointPath;
    private final String apiKey;

    public GeminiClient(
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.endpoint}") String endpointPath
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .build();
        this.endpointPath = endpointPath;
        this.apiKey = apiKey;
    }

    public <T> T sendPrompt(String payload, Class<T> responseType) {
        Mono<String> responseMono = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(endpointPath)
                        .queryParam("key", apiKey)
                        .build())
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(e -> log.error("Failed to call Gemini API", e));

        String responseBody;
        try {
            responseBody = responseMono.block();
            log.info("Gemini API response: {}", responseBody);
        } catch (WebClientResponseException webEx) {
            log.error("Gemini API returned error: {} - {}", webEx.getStatusCode(), webEx.getResponseBodyAsString());
            throw new RuntimeException("Gemini API error: " + webEx.getMessage(), webEx);
        } catch (Exception ex) {
            log.error("Exception during Gemini API call: {}", ex.getMessage(), ex);
            throw new RuntimeException("Exception during Gemini API call", ex);
        }

        try {

            // Parse the response and extract the content (adapt as per actual Gemini response schema)
            JsonNode root = objectMapper.readTree(responseBody);
            String llmJsonText = root.at("/candidates/0/content/parts/0/text").asText();
            // Remove optional markdown block ticks if present (e.g. ```
            if (llmJsonText.startsWith("```json")) {
                llmJsonText = llmJsonText.substring(llmJsonText.indexOf('\n') + 1).trim();
            }
            log.info("Extracted LLM JSON Text: {}", llmJsonText);
            return objectMapper.readValue(llmJsonText, responseType);

        } catch (Exception ex) {
            log.error("Error parsing Gemini LLM response: body=[{}]", responseBody, ex);
            throw new RuntimeException("Error parsing Gemini LLM response", ex);
        }
    }
}
