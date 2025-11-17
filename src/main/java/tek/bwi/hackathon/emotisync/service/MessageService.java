package tek.bwi.hackathon.emotisync.service;

import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.models.UserRole;
import tek.bwi.hackathon.emotisync.repository.MessageRepository;
import tek.bwi.hackathon.emotisync.repository.ThreadRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

        if (UserRole.GUEST.equals(msg.getCreatedBy())) {
            if (msg.getThreadId() != null && !msg.getThreadId().isBlank()) {
                Message savedMsg = messageRepo.save(msg);
                msg.setCreatedBy(UserRole.GUEST);
                msg.setVisibility(List.of(UserRole.GUEST));
                llmService.processGuestMessage(msg, chatHistory);
                return savedMsg;
            } else {
                // First interaction, no thread: create thread, save message, then process LLM
                msg.setTime(Instant.now().toString());
                msg.setThreadId(UUID.randomUUID().toString());
                msg.setCreatedBy(UserRole.GUEST);
                msg.setVisibility(List.of(UserRole.GUEST));
                llmService.processGuestMessage(msg, chatHistory);
                return messageRepo.save(msg);
            }
        } else if (UserRole.STAFF.equals(msg.getCreatedBy()) || UserRole.ADMIN.equals(msg.getCreatedBy())) {
            Message savedMsg = messageRepo.save(msg);
            llmService.processAdminStaffMessage(savedMsg, chatHistory);
            return savedMsg;
        } else {
            throw new IllegalArgumentException("Unknown sender role");
        }
    }


    public List<Message> getByThreadId(String threadId, String userId, String userType) {
        List<Message> userMessages = messageRepo.findByThreadIdAndUserIdOrderByTimeAsc(threadId, userId);
        return userMessages.stream().filter(msg -> msg.getVisibility().contains(UserRole.valueOf(userType))).toList();
    }
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
