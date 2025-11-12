package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired private ReservationService service;

    @PostMapping
    public Reservation create(@RequestBody Reservation reservation) {
        return service.create(reservation);
    }

    @GetMapping
    public List<Reservation> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Reservation getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Reservation> getByUserId(@PathVariable String userId) {
        return service.getByUserId(userId);
    }

    @GetMapping("/room/{roomNumber}")
    public List<Reservation> getByRoomNumber(@PathVariable String roomNumber) {
        return service.getByRoomNumber(roomNumber);
    }

    @GetMapping("/roomtype/{roomType}")
    public List<Reservation> getByRoomType(@PathVariable String roomType) {
        return service.getByRoomType(roomType);
    }

    @GetMapping("/status/{status}")
    public List<Reservation> getByStatus(@PathVariable String status) {
        return service.getByStatus(status);
    }

    @PutMapping("/{id}/status")
    public Reservation updateStatus(@PathVariable String id, @RequestBody String status) {
        return service.updateStatus(id, status);
    }
}

