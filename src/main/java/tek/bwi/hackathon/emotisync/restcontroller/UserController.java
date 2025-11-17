package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired private UserService service;

    @PostMapping
    public UserInfo create(@RequestBody UserInfo user) {
        return service.create(user);
    }
    @GetMapping
    public List<UserInfo> getAll() {
        return service.getAll();
    }
    @GetMapping("/{userId}")
    public UserInfo getById(@PathVariable String userId) {
        return service.getById(userId);
    }
    @GetMapping("/role/{role}")
    public List<UserInfo> getByRole(@PathVariable String role) {
        return service.getByRole(role);
    }
    @GetMapping("/email/{emailId}")
    public List<UserInfo> getByEmail(@PathVariable String emailId) {
        return service.getByEmail(emailId);
    }
}

