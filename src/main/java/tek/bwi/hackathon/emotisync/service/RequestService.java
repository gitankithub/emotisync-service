package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tek.bwi.hackathon.emotisync.entities.Request;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.jpa.MessageRepository;
import tek.bwi.hackathon.emotisync.jpa.RequestRepository;
import tek.bwi.hackathon.emotisync.jpa.ThreadRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RequestService {
    @Autowired private RequestRepository requestRepo;
    @Autowired private ThreadRepository threadRepo;
    @Autowired private MessageRepository messageRepo;

    public Mono<Request> create(Request request) {
        String reqId = UUID.randomUUID().toString();
        request.setId(reqId);
        request.setStatus("open");
        request.setCreatedAt(Instant.now().toString());
        request.setUpdatedAt(Instant.now().toString());

        UserThread guestThread = new UserThread();
        guestThread.setThreadId(UUID.randomUUID().toString());
        guestThread.setRequestId(reqId);
        guestThread.setThreadType("guest");
        guestThread.setParticipantId(request.getSenderId());

        UserThread staffThread = new UserThread();
        staffThread.setThreadId(UUID.randomUUID().toString());
        staffThread.setRequestId(reqId);
        staffThread.setThreadType("staff");
        staffThread.setParticipantId("staffId");

        UserThread adminThread = new UserThread();
        adminThread.setThreadId(UUID.randomUUID().toString());
        adminThread.setRequestId(reqId);
        adminThread.setThreadType("admin");
        adminThread.setParticipantId("adminId");

        return requestRepo.save(request)
                .flatMap(savedRequest ->
                        threadRepo.save(guestThread)
                                .then(threadRepo.save(staffThread))
                                .then(threadRepo.save(adminThread))
                                .then(Mono.just(savedRequest))
                );
    }


    public Flux<Request> getByGuestId(String guestId) { return requestRepo.findByGuestId(guestId); }
    public Mono<Request> getById(String id) { return requestRepo.findById(id); }
    public Mono<Request> updateStatus(String id, String status) {
        return getById(id)
                .flatMap(req -> {
                    req.setStatus(status);
                    req.setUpdatedAt(Instant.now().toString());
                    return requestRepo.save(req);
                });
    }

}
