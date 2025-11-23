package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;

import java.util.List;
import java.util.Optional;


@Repository
public interface RequestRepository extends MongoRepository<ServiceRequest, String> {
    List<ServiceRequest> findByGuestId(String guestId);
    List<ServiceRequest> findByGuestIdAndStatusIn(String guestId, List<String> status);
    ServiceRequest findByUserThread_ThreadId(String threadId);
    Optional<ServiceRequest> findByRequestIdAndStatusIn(String requestId, List<String> status);
}
