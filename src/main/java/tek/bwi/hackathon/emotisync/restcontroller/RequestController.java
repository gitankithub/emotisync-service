package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.Request;
import tek.bwi.hackathon.emotisync.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    @Autowired private RequestService service;
    @PostMapping
    public Mono<Request> create(@RequestBody Request request) { return service.create(request); }
    @GetMapping("/user/{guestId}")
    public Flux<Request> getByGuestId(@PathVariable String guestId) { return service.getByGuestId(guestId); }
    @GetMapping("/{id}")
    public Mono<Request> getById(@PathVariable String id) { return service.getById(id); }
    @PutMapping("/{id}/status")
    public Mono<Request> updateStatus(@PathVariable String id, @RequestBody String status) {
        return service.updateStatus(id, status);
    }
}
