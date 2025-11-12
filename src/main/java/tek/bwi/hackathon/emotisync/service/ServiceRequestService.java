package tek.bwi.hackathon.emotisync.service;

import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.repository.RequestRepository;
import tek.bwi.hackathon.emotisync.models.ThreadParticipant;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceRequestService {
    private final RequestRepository requestRepo;
    private final UserService userService;

    public ServiceRequestService(RequestRepository requestRepo, UserService userService) {
        this.requestRepo = requestRepo;
        this.userService = userService;
    }

    public ServiceRequest create(ServiceRequest request) {
        String reqId = UUID.randomUUID().toString();
        request.setRequestId(reqId);
        request.setStatus("OPEN");
        request.setCreatedAt(Instant.now().toString());
        request.setUpdatedAt(Instant.now().toString());

        UserThread chatThread;
        if (request.getUserThread() != null && request.getUserThread().getThreadId() != null) {
            chatThread = request.getUserThread();
        } else {
            chatThread = new UserThread();
            chatThread.setThreadId(UUID.randomUUID().toString());
            chatThread.setStatus("OPEN");
            chatThread.setCreatedAt(Instant.now().toString());
        }
        chatThread.setRequestId(reqId);
        request.setUserThread(chatThread);
        assignStaffToRequest(request);
        assignThreadParticipants(request.getGuestId(), request.getAssignedTo(), chatThread);

        return requestRepo.save(request);
    }

    private void assignThreadParticipants(String guestId, String assignedTo, UserThread chatThread) {
        List<ThreadParticipant> participants = new ArrayList<>(List.of(new ThreadParticipant(guestId, "GUEST"), new ThreadParticipant(assignedTo, "STAFF")));
        List<UserInfo> availList = userService.getByRole("ADMIN");
        if (availList != null && !availList.isEmpty()) {
            String adminId = availList.get(0).getUserId();
            participants.add(new ThreadParticipant(adminId, "GUEST"));
        }
        chatThread.setParticipantIds(participants);
    }

    private void assignStaffToRequest(ServiceRequest request) {
        List<UserInfo> availList = userService.getByRole("STAFF");
        if (availList != null && !availList.isEmpty()) {
            String assignedStaffId = availList.get(0).getUserId();
            request.setAssignedTo(assignedStaffId);
        }
    }

    public List<ServiceRequest> getByGuestId(String guestId) {
        return requestRepo.findByGuestId(guestId);
    }

    public ServiceRequest getById(String id) {
        return requestRepo.findById(id).orElse(null);
    }

    public ServiceRequest updateStatus(String id, String status) {
        ServiceRequest req = getById(id);
        if (req == null) {
            throw new IllegalArgumentException("ServiceRequest not found with id: " + id);
        }
        req.setStatus(status);
        req.setUpdatedAt(Instant.now().toString());
        if (req.getUserThread() != null) {
            req.getUserThread().setStatus(status);
        }
        return requestRepo.save(req);
    }
}
