package tek.bwi.hackathon.emotisync.jpa;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tek.bwi.hackathon.emotisync.entities.Message;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<Message, String> {
    Flux<Message> findByThreadIdOrderByTimeAsc(String threadId);
}
