package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.jpa.ThreadRepository;

import java.util.List;

@Service
public class ThreadService {
    @Autowired private ThreadRepository repo;

    public Flux<UserThread> getByRequestId(String requestId) { return repo.findByRequestId(requestId); }
    public Mono<UserThread> getById(String id) { return repo.findById(id); }
    public Mono<UserThread> getByRequestIdAndType(String requestId, String threadType) {
        return repo.findByRequestIdAndThreadType(requestId, threadType);
    }
}

