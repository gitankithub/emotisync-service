package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.service.ThreadService;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {
    private final ThreadService service;

    @Autowired
    public ThreadController(ThreadService service) {
        this.service = service;
    }

    @GetMapping("/request/{requestId}")
    public UserThread getByRequestId(@PathVariable String requestId) { return service.getByRequestId(requestId); }
    @GetMapping("/{id}")
    public UserThread getById(@PathVariable String id) { return service.getById(id); }
}