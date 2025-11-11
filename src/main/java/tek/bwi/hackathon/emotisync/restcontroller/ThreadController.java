package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.service.ThreadService;

import java.util.List;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {
    @Autowired private ThreadService service;
    @GetMapping("/request/{requestId}")
    public Flux<UserThread> getByRequestId(@PathVariable String requestId) { return service.getByRequestId(requestId); }
    @GetMapping("/{id}")
    public Mono<UserThread> getById(@PathVariable String id) { return service.getById(id); }
    @GetMapping("/request/{requestId}/type/{threadType}")
    public Mono<UserThread> getByRequestIdAndType(@PathVariable String requestId, @PathVariable String threadType) {
        return service.getByRequestIdAndType(requestId, threadType);
    }
}

