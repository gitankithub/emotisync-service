package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.repository.ThreadRepository;


@Service
public class ThreadService {
    private final ThreadRepository repo;

    @Autowired
    public ThreadService(ThreadRepository repo) {
        this.repo = repo;
    }

    public UserThread getByRequestId(String requestId) { return repo.findByRequestId(requestId); }
    public UserThread getById(String id) { return repo.findById(id).orElse(null); }
}

