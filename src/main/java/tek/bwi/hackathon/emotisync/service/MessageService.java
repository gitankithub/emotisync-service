package tek.bwi.hackathon.emotisync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.client.GeminiClient;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.UserThread;
import tek.bwi.hackathon.emotisync.repository.MessageRepository;
import tek.bwi.hackathon.emotisync.repository.ThreadRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    @Autowired private MessageRepository messageRepo;
    @Autowired private GeminiClient geminiClient;
    @Autowired private LLMService llmService;
    @Autowired private ThreadRepository threadRepository;

    public Message create(Message msg) {
        List<Message> chatHistory = populateChatHistory(msg);

        if ("GUEST".equalsIgnoreCase(msg.getUserRole())) {
            if (msg.getThreadId() != null && !msg.getThreadId().isBlank()) {
                Message savedMsg = messageRepo.save(msg);
                llmService.processGuestMessage(savedMsg, chatHistory);
                return savedMsg;
            } else {
                // First interaction, no thread: create thread, save message, then process LLM
                String newThreadId = UUID.randomUUID().toString();
                UserThread newThread = new UserThread(newThreadId, null, null, "OPEN", String.valueOf(Instant.now()));

                UserThread savedThread = threadRepository.save(newThread);
                msg.setThreadId(savedThread.getThreadId());

                Message savedMsg = messageRepo.save(msg);
                llmService.processGuestMessage(savedMsg, chatHistory);
                return savedMsg;
            }
        } else if ("STAFF".equalsIgnoreCase(msg.getUserRole()) || "ADMIN".equalsIgnoreCase(msg.getUserRole())) {
            Message savedMsg = messageRepo.save(msg);
            llmService.processAdminStaffMessage(savedMsg, chatHistory);
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
