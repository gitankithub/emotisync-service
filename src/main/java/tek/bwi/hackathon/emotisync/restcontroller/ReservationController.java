package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired private ReservationService service;

    @PostMapping
    public Mono<Reservation> create(@RequestBody Reservation reservation) {
        return service.create(reservation);
    }
    @GetMapping
    public Flux<Reservation> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public Mono<Reservation> getById(@PathVariable String id) {
        return service.getById(id);
    }
    @GetMapping("/user/{userId}")
    public Flux<Reservation> getByUserId(@PathVariable String userId) {
        return service.getByUserId(userId);
    }
    @GetMapping("/room/{roomNumber}")
    public Flux<Reservation> getByRoomNumber(@PathVariable String roomNumber) {
        return service.getByRoomNumber(roomNumber);
    }
    @GetMapping("/roomtype/{roomType}")
    public Flux<Reservation> getByRoomType(@PathVariable String roomType) {
        return service.getByRoomType(roomType);
    }
    @GetMapping("/status/{status}")
    public Flux<Reservation> getByStatus(@PathVariable String status) {
        return service.getByStatus(status);
    }
    @PutMapping("/{id}/status")
    public Mono<Reservation> updateStatus(@PathVariable String id, @RequestBody String status) {
        return service.updateStatus(id, status);
    }
}

