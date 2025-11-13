package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    @Autowired
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserInfo create(UserInfo UserInfo) {
        return repo.save(UserInfo);
    }
    public List<UserInfo> getAll() {
        return repo.findAll();
    }
    public UserInfo getById(String UserInfoId) {
        return repo.findById(UserInfoId).orElse(null);
    }
    public List<UserInfo> getByRole(String role) {
        return repo.findByRole(role);
    }
}

