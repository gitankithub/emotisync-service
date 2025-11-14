package tek.bwi.hackathon.emotisync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.models.LLMResponse;
import tek.bwi.hackathon.emotisync.repository.MessageRepository;
import tek.bwi.hackathon.emotisync.repository.RequestRepository;

import java.time.Instant;

@Slf4j
@Service
public class LLMOrchestrationService {

    private final ServiceRequestService serviceRequestService;
    private final RequestRepository requestRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public LLMOrchestrationService(ServiceRequestService serviceRequestService, RequestRepository requestRepository, MessageRepository messageRepository) {
        this.serviceRequestService = serviceRequestService;
        this.requestRepository = requestRepository;
        this.messageRepository = messageRepository;
    }

    public void handleLLMResponse(
            LLMResponse llmResponse,
            Message message) {
        // Check for existing service request linked to this thread
        ServiceRequest existingRequest = requestRepository.findByUserThread_ThreadId(message.getThreadId());
        log.info("Existing Service Request: {}", existingRequest);
        if (llmResponse.isActionNeeded() && llmResponse.getActionDetail() != null) {
            String actionType = llmResponse.getActionDetail().getType();
            switch (actionType.toLowerCase()) {
                case "createservicerequest":
                    if (existingRequest != null) {
                        log.info("ServiceRequest already exists for threadId={}", message.getThreadId());
                        addAllResponseMessages(llmResponse, message, existingRequest);
                        break;
                    }
                    // Create new service request
                    ServiceRequest newRequest = serviceRequestService.create(buildServiceRequest(llmResponse, message));
                    addAllResponseMessages(llmResponse, message, newRequest);
                    break;

                case "escalate":
                    if (existingRequest != null)
                        serviceRequestService.updateStatus(existingRequest.getRequestId(), "ESCALATED");
                    addAllResponseMessages(llmResponse, message, existingRequest);
                    break;

                case "closerequest":
                    if (existingRequest != null)
                        serviceRequestService.updateStatus(existingRequest.getRequestId(), "CLOSED");
                    addAllResponseMessages(llmResponse, message, existingRequest);
                    break;

                default:
                    addAllResponseMessages(llmResponse, message, existingRequest);
            }
        } else {
            addAllResponseMessages(llmResponse, message, existingRequest);
        }
    }

    private ServiceRequest buildServiceRequest(LLMResponse llmResponse, Message message) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setRequestTitle(llmResponse.getActionDetail().getTitle());
        serviceRequest.setRequestDescription(llmResponse.getActionDetail().getDescription());
        serviceRequest.setStatus("OPEN");
        serviceRequest.setGuestId(message.getUserId());
        serviceRequest.setRequestUrgency(llmResponse.getActionDetail().getUrgency());
        UserThread chatThread = new UserThread();
        chatThread.setThreadId(message.getThreadId());
        serviceRequest.setUserThread(chatThread);
        return serviceRequest;
    }

    private void addAllResponseMessages(
            LLMResponse llmResponse,
            Message originalMsg,
            ServiceRequest request) {
        if (nonEmpty(llmResponse.getResponseForGuest())) {
            Message guestMsg = new Message();
            guestMsg.setUserId(originalMsg.getUserId());
            guestMsg.setThreadId(request.getUserThread().getThreadId());
            guestMsg.setContent(llmResponse.getResponseForGuest());
            guestMsg.setUserRole("GUEST");
            guestMsg.setTime(Instant.now().toString());
            messageRepository.save(guestMsg);
        }
        if (nonEmpty(llmResponse.getResponseForStaff())) {
            Message staffMsg = new Message();
            staffMsg.setUserId(request.getAssignedTo());
            staffMsg.setThreadId(request.getUserThread().getThreadId());
            staffMsg.setContent(llmResponse.getResponseForStaff());
            staffMsg.setUserRole("STAFF");
            staffMsg.setTime(Instant.now().toString());
            messageRepository.save(staffMsg);
        }
        if (nonEmpty(llmResponse.getResponseForAdmin())) {
            Message adminMsg = new Message();
            request.getUserThread().getParticipantIds().stream()
                    .filter(participant -> "ADMIN".equalsIgnoreCase(participant.getRole()))
                    .findFirst()
                    .ifPresent(participant -> adminMsg.setUserId(participant.getId()));
            adminMsg.setThreadId(request.getUserThread().getThreadId());
            adminMsg.setContent(llmResponse.getResponseForAdmin());
            adminMsg.setUserRole("ADMIN");
            adminMsg.setTime(Instant.now().toString());
            messageRepository.save(adminMsg);
        }
    }

    private boolean nonEmpty(String val) {
        return val != null && !val.trim().isEmpty();
    }
}

