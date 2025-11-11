package tek.bwi.hackathon.emotisync.jpa;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tek.bwi.hackathon.emotisync.entities.Request;


@Repository
public interface RequestRepository extends ReactiveMongoRepository<Request, String> {
    Flux<Request> findByGuestId(String guestId);
}
