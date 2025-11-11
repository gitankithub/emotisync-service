package tek.bwi.hackathon.emotisync.jpa;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tek.bwi.hackathon.emotisync.entities.Reservation;


@Repository
public interface ReservationRepository extends ReactiveMongoRepository<Reservation, String> {
    Flux<Reservation> findByUserId(String userId);
    Flux<Reservation> findByRoomNumber(String roomNumber);
    Flux<Reservation> findByRoomType(String roomType);
    Flux<Reservation> findByStatus(String status);
}

