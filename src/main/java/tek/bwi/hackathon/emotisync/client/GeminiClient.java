package tek.bwi.hackathon.emotisync.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class GeminiClient {

    @Autowired private final WebClient webClient;

    // Gemini API URL and API key loaded from application.properties
    public GeminiClient(@Value("${gemini.api.url}") String apiUrl,
                        @Value("${gemini.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String sendPrompt(String payload) {

        // Call Gemini LLM endpoint and retrieve response
        Mono<String> responseMono = webClient.post()
                .uri("/v1/generate") // Adapt endpoint path as needed
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10));

        // Blocking here for simplicity, async possible
        String responseBody = "";
        log.info(String.valueOf(responseMono));
        try {
            responseBody = responseMono.block();
        } catch (Exception e) {
            log.error("Error: LLM request failed");
            return "Error: LLM request failed";
        }
        log.info(responseBody);
        // Parse and extract text from the JSON response
        // Assuming response has field: { "generated_text": "..." }
        return responseBody;
    }

}

