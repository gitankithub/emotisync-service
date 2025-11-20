package tek.bwi.hackathon.emotisync.service;


import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.Message;
import tek.bwi.hackathon.emotisync.entities.Reservation;
import tek.bwi.hackathon.emotisync.entities.ServiceRequest;
import tek.bwi.hackathon.emotisync.entities.UserInfo;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromptBuilderService {

    public String buildGuestPrompt(Message message, UserInfo guestDetails, Reservation reservationDetails, List<Message> guestChatHistory) {
        String chatHistorySummary = summarizeChatHistory(guestChatHistory);
        String guestReservationDetails = summarizeGuestDetail(guestDetails, reservationDetails);

        return """
                You are a hotel assistant orchestrating guest service communication.
                                
                Guest Details:
                %s
                Message: "%s"
                Sentiment: (please infer from the guest input)
                                
                Recent Chat with Guest:
                %s
                                
                Your task (conditional):
                - ALWAYS generate a clear, polite response for the guest (responseForGuest).
                - For staff and admin:
                  - If the situation requires staff and/or admin to be notified, generate a specific message (responseForStaff, responseForAdmin). If not, leave these fields empty or omit them.
                  - Only notify parties if there's a clear need based on guest input and context.
                - For service requests:
                  - If the guest's input suggests a new service request (for amenities, issues, or help), set actionNeeded to true, with actionDetail describing what should be done.
                  - If an escalation or closure is needed, set actionNeeded to true and in actionDetail specify 'type' as escalate or closeRequest, and 'escalationTarget' as "serviceRequest", "thread", or "both", indicating whether escalation applies to the service request, the thread, or both.
                  - If not, set actionNeeded to false and skip extra fields.
                                
                Return this structured JSON:
                {
                  "responseForGuest": "Response to guest, if needed",
                  "responseForStaff": "Response to staff, if needed",
                  "responseForAdmin": "Response or escalation instruction for admin, if needed",
                  "actionNeeded": true/false,
                  "actionDetail": {
                    "type": "...",           // e.g. "ESCALATE",  "CREATE_SERVICE_REQUEST",  "COMPLETED", "CLOSE_REQUEST"
                    "targetUserRole": "...", // e.g. "staff", "admin"
                    "escalationTarget": "serviceRequest|thread|both",
                    "title": "...",
                    "description": "...",
                    "urgency": "routine|urgent|escalated"
                  }
                }
                """.formatted(guestReservationDetails, message.getContent(), chatHistorySummary);
    }

    public String buildStaffPrompt(Message message, UserInfo userInfo, UserInfo guestInfo, Reservation guestReservation, List<Message> staffChatHistory) {
        String chatHistorySummary = summarizeChatHistory(staffChatHistory);
        String guestReservationDetails = summarizeGuestDetail(guestInfo, guestReservation);
        return """
                You are assisting hotel staff managing guest service requests.
                 
                 Staff Name: %s
                 Message: "%s"
                 Guest Details:
                 %s
                 Recent Staff Chat:
                 %s
                 
                 Your task (conditional):
                 - Carefully analyze the staff's input and context.
                 - If appropriate, generate:
                     - A message for the staff (responseForStaff).
                     - A message for the guest if the guest should be notified (responseForGuest).
                     - A message for admin if escalation is needed (responseForAdmin).
                 - If the staff's response implies a backend/service action (such as marking a service request complete, escalating, starting a new request, or updating any entity), set "actionNeeded" to true and provide "actionType" and "actionDetails" explaining what should happen.
                 - Only include each field if relevant.
                 
                 Return JSON:
                 {
                    "responseForGuest": "Response to guest, if needed",
                    "responseForStaff": "Response to staff, if needed",
                    "responseForAdmin": "Response or escalation instruction for admin, if needed",
                    "actionNeeded": true/false,
                    "actionDetail": {
                      "type": "...",          // e.g. "ESCALATE",  "CREATE_SERVICE_REQUEST",  "COMPLETED", "CLOSE_REQUEST"
                      "targetUserRole": "...",
                      "description": "...",
                      "urgency": "routine|urgent|escalated"
                    }
                  }
                """.formatted(userInfo.getName(), message.getContent(), guestReservationDetails, chatHistorySummary);
    }

    public String buildAdminPrompt(Message message, UserInfo userInfo, UserInfo guestInfo, Reservation guestReservation, List<Message> adminChatHistory) {
        String chatHistorySummary = summarizeChatHistory(adminChatHistory);
        String guestReservationDetails = summarizeGuestDetail(guestInfo, guestReservation);
        return """
                You are assisting hotel admin overseeing operations and escalations.
                 
                 Admin Name: %s
                 Message: "%s"
                 
                 Guest Details:
                 %s
                 Relevant Chat History:
                 %s
                 
                 Your task (conditional – only include fields if needed):
                 - Analyze admin updates or escalation requests.
                 - If the context requires a reply to staff, generate responseForStaff.
                 - If a guest should be notified based on admin action, generate responseForGuest.
                 - If an admin-originated service action, policy change, escalation, or other backend action is implied, set "actionNeeded" to true and fill in "actionType" and "actionDetails" with actionable instructions for backend processing.
                 - All fields/messages should be included only when meaningful in context.
                 - Advise logical next steps for hotel/resolution work.
                 
                 Return this structured JSON object:
                 
                 {
                    "responseForGuest": "Response to guest, if needed",
                    "responseForStaff": "Response to staff, if needed",
                    "responseForAdmin": "Response or escalation instruction for admin, if needed",
                    "actionNeeded": true/false,
                    "actionDetail": {
                      "type": "...",
                      "targetUserRole": "...",
                      "description": "...",
                      "urgency": "routine|urgent|escalated"
                    }
                  }
                """.formatted(userInfo.getName(), message.getContent(), guestReservationDetails, chatHistorySummary);
    }

    public String buildChatQueryPrompt(String chatQuery, ServiceRequest bestMatch, double bestScore, Reservation reservation, UserInfo userInfo) {
        String prompt;
        String guestReservationDetails = summarizeGuestDetail(userInfo, reservation);
        if (bestMatch != null && bestScore > 0.8) {
            prompt = """
                    You are assisting hotel guest for guest service requests or general query.
                    Guest Details:
                    %s
                    Message: "%s"
                    Sentiment: (please infer from the guest input)
                    Related open request found:
                    Title: %s
                    Description: %s
                    Current Status: %s
                    
                    Based on the guest's question and the related open request, determine the appropriate action.
                    Possible actions:
                    - SERVICE_REQUEST: If the guest is asking for new service or information related to the existing request. Provide response to guest, staff and admin if needed.
                    - ESCALATE: If the guest is dissatisfied or requests urgent attention. Response to staff and admin needed.
                    - COMPLETE: If the guest indicates the issue is resolved. Response to staff and admin if needed.
                    - DELAY: If the guest reports a delay or ongoing issue. Response to staff and admin if needed.
                    - NORMAL: If no action is needed.
                    
                    Respond with JSON: {"action":"SERVICE_REQUEST|ESCALATE|COMPLETE|DELAY|NORMAL","replyToGuest":"Response to guest","replyToStaff":"Response to Staff","replyToAdmin":"Response to admin"}
                    """.formatted(
                    guestReservationDetails,
                    chatQuery,
                    bestMatch.getRequestTitle(),
                    bestMatch.getRequestDescription() != null ? bestMatch.getRequestDescription() : "",
                    bestMatch.getStatus());
        } else {
            prompt = """
                    You are assisting hotel guest for guest service requests or general query.
                    Guest Details:
                    %s
                    Message: "%s"
                    Sentiment: (please infer from the guest input)
                    No related open requests. Respond with help or new request option as JSON: { "action": "...", "replyToGuest": "..." }
                    """.formatted(guestReservationDetails, chatQuery);
        }
        return prompt;
    }

    public String summarizeChatHistory(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "No prior messages.";
        }
        return messages.stream()
                .map(m -> m.getUserId() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }
    private String summarizeGuestDetail(UserInfo guestDetails, Reservation reservationDetails) {
        return """
                Name: %s
                Reservation ID: %s
                Room Number: %s
                Room Type: %s
                Check-In Date: %s
                Check-Out Date: %s
                Number of Occupants: %d
                Status: %s
                """.formatted(
                guestDetails.getName(),
                reservationDetails.getId(),
                reservationDetails.getRoomNumber(),
                reservationDetails.getRoomType(),
                reservationDetails.getCheckInDate(),
                reservationDetails.getCheckOutDate(),
                reservationDetails.getNumberOfOccupants(),
                reservationDetails.getStatus());
    }
}

