package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRequestIdOrderByTimestampAsc(String threadId);
}