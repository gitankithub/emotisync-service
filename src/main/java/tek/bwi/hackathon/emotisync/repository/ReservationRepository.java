package tek.bwi.hackathon.emotisync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tek.bwi.hackathon.emotisync.entities.Reservation;

import java.util.List;


@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {
    List<Reservation> findByUserId(String userId);
    List<Reservation> findByRoomNumber(String roomNumber);
    List<Reservation> findByRoomType(String roomType);
    List<Reservation> findByStatus(String status);
    Reservation findByUserIdAndStatus(String userId, String status);
}

