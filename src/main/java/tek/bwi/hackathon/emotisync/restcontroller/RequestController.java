package tek.bwi.hackathon.emotisync.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.service.ServiceRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    @Autowired private ServiceRequestService serviceRequestService;
    @PostMapping
    public ServiceRequest create(@RequestBody ServiceRequest request) { return serviceRequestService.create(request); }
    @GetMapping("/user/{guestId}")
    public List<ServiceRequest> getByGuestId(@PathVariable String guestId) { return serviceRequestService.getByGuestId(guestId); }
    @GetMapping("/{id}")
    public ServiceRequest getById(@PathVariable String id) { return serviceRequestService.getById(id); }
    @PutMapping("/{id}/status")
    public ServiceRequest updateStatus(@PathVariable String id, @RequestBody String status) {
        return serviceRequestService.updateStatus(id, status);
    }
}
