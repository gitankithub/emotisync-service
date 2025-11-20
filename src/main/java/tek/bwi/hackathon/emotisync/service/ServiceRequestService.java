package tek.bwi.hackathon.emotisync.service;

import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.entities.UserInfo;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.models.ServiceRequestStatus;
import tek.bwi.hackathon.emotisync.models.ThreadParticipant;
import tek.bwi.hackathon.emotisync.repository.RequestRepository;
import tek.bwi.hackathon.emotisync.repository.ThreadRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class ServiceRequestService {
    private final RequestRepository requestRepo;
    private final UserService userService;
    private final ThreadRepository threadRepository;

    public ServiceRequestService(RequestRepository requestRepo, UserService userService, ThreadRepository threadRepository) {
        this.requestRepo = requestRepo;
        this.userService = userService;
        this.threadRepository = threadRepository;
    }

    public ServiceRequest create(ServiceRequest request) {
        // Set basic fields (requestId auto-assigned with save)
        String reqId = buildRequestNumber();
        request.setRequestId(reqId);
        request.setStatus(ServiceRequestStatus.OPEN);
        request.setCreatedAt(Instant.now().toString());
        request.setUpdatedAt(Instant.now().toString());
        UserThread chatThread = request.getUserThread();
        chatThread.setStatus(ServiceRequestStatus.OPEN);
        chatThread.setCreatedAt(Instant.now().toString());
        chatThread.setRequestId(reqId);
        assignStaffToRequest(request, chatThread);
        assignThreadParticipants(request.getGuestId(), request.getAssignedTo(), chatThread);
        chatThread = threadRepository.save(chatThread);

        request.setUserThread(chatThread);
        return requestRepo.save(request);
    }

    private String buildRequestNumber() {
        long timestamp = System.currentTimeMillis() % 10000000000L; // last 10 digits of current time in ms
        Random random = new Random();
        int randomNum = random.nextInt(900) + 100;  // 3-digit random number (100-999)
        // Format: REQ + 10-digit timestamp + 3-digit random number = 13 digits total
        return String.format("REQ%010d%03d", timestamp, randomNum);
    }

    private void assignThreadParticipants(String guestId, String assignedTo, UserThread chatThread) {
        List<ThreadParticipant> participants = new ArrayList<>();
        participants.add(new ThreadParticipant(guestId, "GUEST"));
        participants.add(new ThreadParticipant(assignedTo, "STAFF"));

        List<UserInfo> adminList = userService.getByRole("ADMIN");
        if (adminList != null && !adminList.isEmpty()) {
            String adminId = adminList.get(0).getUserId();
            participants.add(new ThreadParticipant(adminId, "ADMIN")); // Fixed role from "GUEST" to "ADMIN"
        }
        chatThread.setParticipantIds(participants);
    }

    private void assignStaffToRequest(ServiceRequest request, UserThread chatThread) {
        List<UserInfo> availList = userService.getByRole("STAFF");
        if (availList != null && !availList.isEmpty()) {
            String assignedStaffId = availList.get(0).getUserId();
            request.setAssignedTo(assignedStaffId);
            request.setStatus(ServiceRequestStatus.ASSIGNED);
            chatThread.setStatus(ServiceRequestStatus.ASSIGNED);
        } else {
            request.setAssignedTo(null);
            request.setStatus(ServiceRequestStatus.OPEN);
            chatThread.setStatus(ServiceRequestStatus.OPEN);
        }
    }
    public List<ServiceRequest> getAll() {
        return requestRepo.findAll();
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
        req.setStatus(ServiceRequestStatus.valueOf(status));
        req.setUpdatedAt(Instant.now().toString());
        if (req.getUserThread() != null) {
            req.getUserThread().setStatus(ServiceRequestStatus.valueOf(status));
        }
        return requestRepo.save(req);
    }
}
