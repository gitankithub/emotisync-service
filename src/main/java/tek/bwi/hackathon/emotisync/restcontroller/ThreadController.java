package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.service.ThreadService;

import java.util.List;

@RestController
@RequestMapping("/api/threads")
public class ThreadController {
    @Autowired private ThreadService service;
    @GetMapping("/request/{requestId}")
    public List<UserThread> getByRequestId(@PathVariable String requestId) { return service.getByRequestId(requestId); }
    @GetMapping("/{id}")
    public UserThread getById(@PathVariable String id) { return service.getById(id); }
}

