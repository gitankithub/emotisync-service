package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.jpa.MessageRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    @Autowired private MessageRepository repo;

    @Autowired private GeminiClient geminiClient;

    public Mono<Message> create(Message msg) {
        msg.setId(UUID.randomUUID().toString());
        msg.setTime(Instant.now().toString());
        return repo.save(msg);
    }
    public Flux<Message> getByThreadId(String threadId) { return repo.findByThreadIdOrderByTimeAsc(threadId); }
    public Mono<Message> getById(String id) { return repo.findById(id); }
}
