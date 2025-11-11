package tek.bwi.hackathon.emotisync.jpa;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.UserThread;


@Repository
public interface ThreadRepository extends ReactiveMongoRepository<UserThread, String> {
    Flux<UserThread> findByRequestId(String requestId);
    Mono<UserThread> findByRequestIdAndThreadType(String requestId, String threadType);
}
