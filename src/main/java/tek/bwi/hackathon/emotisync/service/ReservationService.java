package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.jpa.ReservationRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    @Autowired private ReservationRepository repo;

    public Mono<Reservation> create(Reservation reservation) {
        reservation.setId(UUID.randomUUID().toString());
        reservation.setCreatedAt(Instant.now().toString());
        reservation.setUpdatedAt(Instant.now().toString());
        return repo.save(reservation);
    }
    public Flux<Reservation> getAll() {
        return repo.findAll();
    }
    public Mono<Reservation> getById(String id) {
        return repo.findById(id);
    }
    public Flux<Reservation> getByUserId(String userId) {
        return repo.findByUserId(userId);
    }
    public Flux<Reservation> getByRoomNumber(String roomNumber) {
        return repo.findByRoomNumber(roomNumber);
    }
    public Flux<Reservation> getByRoomType(String roomType) {
        return repo.findByRoomType(roomType);
    }
    public Flux<Reservation> getByStatus(String status) {
        return repo.findByStatus(status);
    }
    public Mono<Reservation> updateStatus(String id, String status) {
        return getById(id)
                .flatMap(res -> {
                    res.setStatus(status);
                    res.setUpdatedAt(Instant.now().toString());
                    return repo.save(res);
                });
    }

}

