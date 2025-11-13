package tek.bwi.hackathon.emotisync.service;

import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.repository.MessageRepository;
import tek.bwi.hackathon.emotisync.repository.ThreadRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepo;
    private final LLMService llmService;
    private final ThreadRepository threadRepository;

    public MessageService(MessageRepository messageRepo, LLMService llmService, ThreadRepository threadRepository) {
        this.messageRepo = messageRepo;
        this.llmService = llmService;
        this.threadRepository = threadRepository;
    }

    public Message create(Message msg) {
        List<Message> chatHistory = populateChatHistory(msg);

        if ("GUEST".equalsIgnoreCase(msg.getUserRole())) {
            if (msg.getThreadId() != null && !msg.getThreadId().isBlank()) {
                Message savedMsg = messageRepo.save(msg);
                ServiceRequest serviceRequest = llmService.processGuestMessage(msg, chatHistory);
                msg.setThreadId(serviceRequest.getUserThread().getThreadId());
                return savedMsg;
            } else {
                // First interaction, no thread: create thread, save message, then process LLM
                msg.setTime(Instant.now().toString());
                ServiceRequest serviceRequest = llmService.processGuestMessage(msg, chatHistory);
                msg.setThreadId(serviceRequest.getUserThread().getThreadId());
                return messageRepo.save(msg);
            }
        } else if ("STAFF".equalsIgnoreCase(msg.getUserRole()) || "ADMIN".equalsIgnoreCase(msg.getUserRole())) {
            Message savedMsg = messageRepo.save(msg);
            ServiceRequest serviceRequest = llmService.processAdminStaffMessage(savedMsg, chatHistory);
            msg.setThreadId(serviceRequest.getUserThread().getThreadId());
            return savedMsg;
        } else {
            throw new IllegalArgumentException("Unknown sender role");
        }
    }


    public List<Message> getByThreadId(String threadId) { return messageRepo.findByThreadIdOrderByTimeAsc(threadId); }
    public Message getById(String id) { return messageRepo.findById(id).orElse(null); }
    private List<Message> populateChatHistory(Message message) {
        UserThread thread = threadRepository.findByThreadIdAndStatus(message.getThreadId(), "OPEN");
        if (thread != null) {
            return messageRepo.findByThreadIdOrderByTimeAsc(thread.getThreadId());
        } else {
            return Collections.emptyList();
        }
    }

}
