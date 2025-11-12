package tek.bwi.hackathon.emotisync.service;

import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.StaffAvailability;
import tek.bwi.hackathon.emotisync.repository.StaffAvailabilityRepo;

import java.time.Instant;
import java.util.List;

@Service
public class StaffAvailabilityService {
    private final StaffAvailabilityRepo repo;

    public StaffAvailabilityService(StaffAvailabilityRepo repo) {
        this.repo = repo;
    }

    public StaffAvailability setAvailable(String staffId, Instant startTime, Instant endTime) {
        StaffAvailability avail = new StaffAvailability();
        avail.setStaffId(staffId);
        avail.setStatus("AVAILABLE");
        avail.setStartTime(startTime);
        avail.setEndTime(endTime);
        return repo.save(avail);
    }

    // Called when a staff is assigned a service request
    public StaffAvailability setBusy(String staffId, String requestId) {
        StaffAvailability staffAvailability = repo.findByStaffId(staffId);
        if(staffAvailability != null) {
            staffAvailability.setStatus("BUSY");
            staffAvailability.setCurrentRequestId(requestId);
            return repo.save(staffAvailability);
        } else {
            throw new RuntimeException("Staff not found");
        }
    }

    public StaffAvailability setAvailableFromBusy(String staffId) {
        StaffAvailability avail = repo.findByStaffId(staffId);
        if (avail == null) {
            throw new IllegalArgumentException("StaffAvailability not found for staffId: " + staffId);
        }
        avail.setStatus("AVAILABLE");
        avail.setCurrentRequestId(null);
        return repo.save(avail);
    }

    // Finds all available staff (optionally by skill/department)
    public List<StaffAvailability> getAvailableStaff() {
        return repo.findByStatus("AVAILABLE");
    }
}

