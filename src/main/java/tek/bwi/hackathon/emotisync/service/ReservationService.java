package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.repository.ReservationRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {
    @Autowired private ReservationRepository repo;

    public Reservation create(Reservation reservation) {
        reservation.setId(UUID.randomUUID().toString());
        reservation.setCreatedAt(Instant.now().toString());
        reservation.setUpdatedAt(Instant.now().toString());
        return repo.save(reservation);
    }
    public List<Reservation> getAll() {
        return repo.findAll();
    }
    public Reservation getById(String id) {
        return repo.findById(id).orElse(null);
    }
    public List<Reservation> getByUserId(String userId) {
        return repo.findByUserId(userId);
    }
    public List<Reservation> getByRoomNumber(String roomNumber) {
        return repo.findByRoomNumber(roomNumber);
    }

    public List<Reservation> getByRoomType(String roomType) {
        return repo.findByRoomType(roomType);
    }

    public List<Reservation> getByStatus(String status) {
        return repo.findByStatus(status);
    }

    public Reservation updateStatus(String id, String status) {
        Reservation reservation = getById(id);
        if (reservation != null) {
            reservation.setStatus(status);
            reservation.setUpdatedAt(Instant.now().toString());
            return repo.save(reservation);
        } else {
            throw new IllegalArgumentException("Reservation not found with id: " + id);
        }
    }
}