package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired private UserService service;

    @PostMapping
    public Mono<UserInfo> create(@RequestBody UserInfo user) {
        return service.create(user);
    }
    @GetMapping
    public Flux<UserInfo> getAll() {
        return service.getAll();
    }
    @GetMapping("/{userId}")
    public Mono<UserInfo> getById(@PathVariable String userId) {
        return service.getById(userId);
    }
    @GetMapping("/role/{role}")
    public Flux<UserInfo> getByRole(@PathVariable String role) {
        return service.getByRole(role);
    }
}

