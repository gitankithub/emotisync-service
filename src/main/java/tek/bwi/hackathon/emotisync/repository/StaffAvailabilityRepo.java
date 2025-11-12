package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.StaffAvailability;

import java.util.List;

@Repository
public interface StaffAvailabilityRepo extends MongoRepository<StaffAvailability, String> {
    StaffAvailability findByStaffId(String staffId);
    List<StaffAvailability> findByStatus(String status);
}

