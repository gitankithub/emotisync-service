package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired private MessageService service;
    @PostMapping
    public Mono<Message> create(@RequestBody Message msg) { return service.create(msg); }
    @GetMapping("/thread/{threadId}")
    public Flux<Message> getByThreadId(@PathVariable String threadId) { return service.getByThreadId(threadId); }
    @GetMapping("/{id}")
    public Mono<Message> getById(@PathVariable String id) { return service.getById(id); }
}
