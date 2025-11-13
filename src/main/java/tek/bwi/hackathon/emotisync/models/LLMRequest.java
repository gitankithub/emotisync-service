package tek.bwi.hackathon.emotisync.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {
    private List<LLMPayload> contents;
}
