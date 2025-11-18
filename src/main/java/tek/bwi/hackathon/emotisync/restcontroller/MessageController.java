package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired private MessageService service;
    @PostMapping
    public Message create(@RequestBody Message msg) {
        try {
            return service.create(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/thread/{threadId}")
    public List<Message> getByThreadId(@PathVariable String threadId, @RequestParam(required = false) String userId, @RequestParam String userType) { return service.getByThreadId(threadId, userId, userType); }

    @GetMapping("/{id}")
    public Message getById(@PathVariable String id) { return service.getById(id); }
}
