package tek.bwi.hackathon.emotisync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.models.GeminiContent;
import tek.bwi.hackathon.emotisync.models.GeminiPart;
import tek.bwi.hackathon.emotisync.models.GeminiRequest;

import java.util.List;

@Service
public class GeminiChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_MODEL = "gemini-2.0-flash";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PromptBuilderService promptBuilder;
    private final GeminiClient geminiClient;

    @Autowired
    public GeminiChatService(RestTemplate restTemplate, ObjectMapper objectMapper, PromptBuilderService promptBuilder, GeminiClient geminiClient) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.geminiClient = geminiClient;
    }

    public String processMessage(String role, String senderName, String message, List<Message> chatHistory) throws Exception {
        String prompt;
        if ("guest".equalsIgnoreCase(role)) {
            // LLM is to infer sentiment
            prompt = promptBuilder.buildGuestPrompt(senderName, message, chatHistory);
        } else if ("staff".equalsIgnoreCase(role)) {
            prompt = promptBuilder.buildStaffPrompt(senderName, message, chatHistory);
        } else if ("admin".equalsIgnoreCase(role)) {
            prompt = promptBuilder.buildAdminPrompt(senderName, message, chatHistory);
        } else {
            throw new IllegalArgumentException("Invalid user role");
        }

        GeminiRequest geminiRequest = new GeminiRequest(List.of(
                new GeminiContent(List.of(new GeminiPart(prompt)))
        ));
        String payload = objectMapper.writeValueAsString(geminiRequest);
        return geminiClient.sendPrompt(payload);
    }

    private JsonNode callGeminiApi(String requestBody) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Gemini API call failed: " + response.getBody());
        }

        // Parse and extract the generated output, which should be the JSON string
        JsonNode root = objectMapper.readTree(response.getBody());
        // The structure depends on the actual Gemini response: adapt this extraction as needed
        // Usually the model returns the output in: root["candidates"][0]["content"]["parts"][0]["text"]
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            String output = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            return objectMapper.readTree(output); // Should parse as final JSON response per your prompt
        }
        throw new RuntimeException("No valid output found from Gemini model");
    }
}
