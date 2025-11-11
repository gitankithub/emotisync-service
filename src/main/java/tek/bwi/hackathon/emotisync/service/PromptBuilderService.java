package tek.bwi.hackathon.emotisync.service;


import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Message;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromptBuilderService {

    public String buildGuestPrompt(String guestName, String guestMessage, List<Message> guestChatHistory) {
        String chatHistorySummary = summarizeChatHistory(guestChatHistory);

        return """
                You are a hotel assistant responding to guest messages.

                Guest Name: %s
                Message: "%s"
                Sentiment: (Determine the sentiment from guest input)

                Recent Chat with Guest:
                %s

                Your task:
                - Infer guest’s intent and feelings including sentiment.
                - Provide a clear, polite, empathetic reply.
                - Specify who should be informed or notified (e.g., 'staff', 'admin', 'none').
                - Suggest if further help is needed (notify staff, escalate).

                Return JSON:

                {
                  "responseMessage": "Text for guest response",
                  "notify": ["staff", "admin", "none"]
                }
                """.formatted(guestName, guestMessage, chatHistorySummary);
    }

    public String buildStaffPrompt(String staffName, String staffMessage, List<Message> staffChatHistory) {
        String chatHistorySummary = summarizeChatHistory(staffChatHistory);

        return """
                You are assisting hotel staff managing guest service requests.

                Staff Name: %s
                Message: "%s"

                Recent Staff Chat:
                %s

                Your task:
                - Understand staff status or queries.
                - Provide guidance or auto-responses.
                - Specify who to notify if needed (e.g., 'admin', 'guest', 'none').
                - Suggest if escalation or admin notification is needed.

                Return JSON:

                {
                  "responseMessage": "Text for staff response",
                  "notify": ["admin", "guest", "none"]
                }
                """.formatted(staffName, staffMessage, chatHistorySummary);
    }

    public String buildAdminPrompt(String adminName, String adminMessage, List<Message> adminChatHistory) {
        String chatHistorySummary = summarizeChatHistory(adminChatHistory);

        return """
                You are assisting hotel admin overseeing operations and escalations.

                Admin Name: %s
                Message: "%s"

                Relevant Chat History:
                %s

                Your task:
                - Interpret admin updates or escalation requests.
                - Advise next steps or respond accordingly.
                - Specify who should be informed (staff, guest, none).

                Return JSON:

                {
                  "responseMessage": "Text for admin response",
                  "notify": ["staff", "guest", "none"]
                }
                """.formatted(adminName, adminMessage, chatHistorySummary);
    }

    public String summarizeChatHistory(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "No prior messages.";
        }
        return messages.stream()
                .map(m -> m.getSenderId() + ": " + m.getMessage())
                .collect(Collectors.joining("\n"));
    }
}

