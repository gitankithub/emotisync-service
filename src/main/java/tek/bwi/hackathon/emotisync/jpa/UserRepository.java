package tek.bwi.hackathon.emotisync.jpa;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tek.bwi.hackathon.emotisync.entities.UserInfo;


@Repository
public interface UserRepository extends ReactiveMongoRepository<UserInfo, String> {
    Flux<UserInfo> findByRole(String role);
}

