package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;

import java.util.List;


@Repository
public interface RequestRepository extends MongoRepository<ServiceRequest, String> {
    List<ServiceRequest> findByGuestId(String guestId);
    ServiceRequest findByUserThread_ThreadId(String threadId);
}
