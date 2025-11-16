package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.ChatRequest;

import java.util.List;

@Repository
public interface ChatRequestRepository extends MongoRepository<ChatRequest, String> {
    List<ChatRequest> findByGuestIdAndStatus(String guestId, String status);
}



