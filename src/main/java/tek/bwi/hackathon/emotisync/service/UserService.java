package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.jpa.UserRepository;

import java.util.List;

@Service
public class UserService {
    @Autowired private UserRepository repo;

    public Mono<UserInfo> create(UserInfo UserInfo) {
        return repo.save(UserInfo);
    }
    public Flux<UserInfo> getAll() {
        return repo.findAll();
    }
    public Mono<UserInfo> getById(String UserInfoId) {
        return repo.findById(UserInfoId);
    }
    public Flux<UserInfo> getByRole(String role) {
        return repo.findByRole(role);
    }
}

