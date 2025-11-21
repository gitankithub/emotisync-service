package tek.bwi.hackathon.emotisync.service;


import org.springframework.stereotype.Service;
import tek.bwi.hackathon.emotisync.entities.*;

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
                  - If a closure is needed, set actionNeeded to true and in actionDetail specify 'type' as closeRequest.
                  - If not, set actionNeeded to false and skip extra fields.
                                
                Return this structured JSON:
                {
                  "responseForGuest": "Response to guest, if needed",
                  "responseForStaff": "Response to staff, if needed",
                  "responseForAdmin": "Response or escalation instruction for admin, if needed",
                  "actionNeeded": true/false,
                  "actionDetail": {
                    "type": "CREATE_SERVICE_REQUEST|UPDATE_SERVICE_REQUEST|CLOSE_REQUEST",
                    "status": "...", // e.g. "staff", "admin"
                    "escalationTarget": "serviceRequest|thread|both",
                    "title": "...",
                    "description": "...",
                    "urgency": "routine|urgent"
                  }
                }
                """.formatted(guestReservationDetails, message.getContent(), chatHistorySummary);
    }

    public String buildStaffPrompt(Message message, UserInfo userInfo, UserInfo guestInfo, Reservation guestReservation, ServiceRequest serviceRequest, List<Message> chatHistory) {
        String chatHistorySummary = summarizeChatHistory(chatHistory);
        String recentRequestSummary = summarizeServiceRequest(serviceRequest);
        String guestReservationDetails = summarizeGuestDetail(guestInfo, guestReservation);
        return """
                You are assisting hotel staff managing guest service requests.
                                
                Staff Name: %s
                Staff Message: "%s"
                Guest Details:
                %s
                Related request:
                %s
                Recent Chat:
                %s
                                
                Your task (conditional):
                - When staff selects an action for a service request (accept, complete, cancel, etc.), always set "actionNeeded": true and fill "actionDetail" with:
                    - "type": "UPDATE_REQUEST"
                    - "status": corresponding to the staff action (e.g., "IN_PROGRESS", "COMPLETED", "CANCELED")
                    - "targetUserRole": "staff"
                    - "description": summarize what happened
                    - "urgency": as appropriate
                - ESCALATED: If service request SLA is breached. Decide based on the service request createdAt field with standard SLA (You can set SLA based on task type and standard SLA). Response to guest, staff and admin needed.
                - Generate a clear response for staff if case of any action taken (responseForStaff).
                - Generate appropriate messages for guest upon each service request update if make sense.
                - Only include each field if relevant.
                                
                Return JSON:
                {
                  "responseForGuest": "...",
                  "responseForStaff": "...",
                  "responseForAdmin": "...",
                  "actionNeeded": true/false,
                  "actionDetail": {
                    "type": "...", e.g. UPDATE_SERVICE_REQUEST
                    "status": "...", e.g. ACCEPT|IN_PROGRESS|REJECTED|CANCELLED|REASSIGNED|COMPLETED|ESCALATED
                    "targetUserRole": "...",
                    "description": "...",
                    "urgency": "routine|urgent|escalated"
                  }
                }
                """.formatted(userInfo.getName(), message.getContent(), guestReservationDetails, recentRequestSummary, chatHistorySummary);
    }

    private String summarizeServiceRequest(ServiceRequest request) {
        return """
        Related request:
        Title: %s
        Description: %s
        Current Status: %s
        Created At: %s
        """.formatted(
                request.getRequestTitle() != null ? request.getRequestTitle() : "",
                request.getRequestDescription() != null ? request.getRequestDescription() : "",
                request.getStatus() != null ? request.getStatus() : "",
                request.getCreatedAt() != null ? request.getCreatedAt() : "");
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
                    Created At: %s
                    
                    Based on the guest's question and the related open request, determine the appropriate action.
                    Possible actions:
                    - SERVICE_REQUEST: If the guest is asking for new service or information related to the existing request. Provide response to guest, staff and admin if needed.
                    - ESCALATE: If the guest is dissatisfied or requests urgent attention and service SLA is breached. Decide based on the service request createdAt field with standard SLA (You can set SLA based on task type and standard SLA). Response to staff and admin needed.
                    - COMPLETE: If the guest indicates the issue is resolved. Response to staff and admin if needed.
                    - DELAY: If the guest reports a delay or ongoing issue. Response to staff and admin if needed.
                    - NORMAL: If no action is needed.
                    
                    Respond with JSON: {"action":"SERVICE_REQUEST|ESCALATE|COMPLETE|DELAY|NORMAL","replyToGuest":"Response to guest","replyToStaff":"Response to Staff","replyToAdmin":"Response to admin"}
                    """.formatted(
                    guestReservationDetails,
                    chatQuery,
                    bestMatch.getRequestTitle(),
                    bestMatch.getRequestDescription() != null ? bestMatch.getRequestDescription() : "",
                    bestMatch.getStatus(),
                    bestMatch.getCreatedAt());
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
                .map(m -> {
                    String roleLabel = m.getCreatedBy() != null ? "[" + m.getCreatedBy().name() + "]" : "[USER]";
                    String timeLabel = (m.getTime() != null && !m.getTime().isEmpty()) ? "[" + m.getTime() + "] " : "";
                    String mainText = m.getContent() != null ? m.getContent() : "";
                    String feedbackLabel = "";
                    feedbackLabel = getFeedbackLabel(m, feedbackLabel);
                    return timeLabel + roleLabel + ": " + mainText + feedbackLabel;
                })
                .collect(Collectors.joining("\n"));
    }

    private static String getFeedbackLabel(Message m, String feedbackLabel) {
        GuestFeedback feedback = m.getGuestFeedback();
        if (feedback != null && feedback.getRating() != null && !feedback.getRating().isEmpty()) {
            String rating = feedback.getRating();
            String text = feedback.getFeedbackText();
            if (text != null && !text.isEmpty()) {
                feedbackLabel = String.format(" (Feedback rating: %s, \"%s\")", rating, text);
            } else {
                feedbackLabel = String.format(" (Feedback rating: %s)", rating);
            }
        }
        return feedbackLabel;
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

