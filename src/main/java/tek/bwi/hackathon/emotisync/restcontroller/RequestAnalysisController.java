package tek.bwi.hackathon.emotisync.restcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.models.RequestAnalysis;
import tek.bwi.hackathon.emotisync.service.RequestAnalysisService;

@RestController
@RequestMapping("/api/request-analysis")
public class RequestAnalysisController {
    private final RequestAnalysisService requestAnalysisService;

    public RequestAnalysisController(RequestAnalysisService requestAnalysisService) {
        this.requestAnalysisService = requestAnalysisService;
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RequestAnalysis> analyze(@PathVariable String requestId) {
        RequestAnalysis response = null;
        try {
            response = requestAnalysisService.getOrRunAnalysis(requestId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(response);
    }
}

