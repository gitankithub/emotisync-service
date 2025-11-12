package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.UserThread;

import java.util.List;


@Repository
public interface ThreadRepository extends MongoRepository<UserThread, String> {
    List<UserThread> findByRequestId(String requestId);
    UserThread findByThreadId(String threadId);
    UserThread findByThreadIdAndStatus(String threadId, String open);
}
