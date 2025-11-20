package tek.bwi.hackathon.emotisync.models;

import lombok.Getter;

@Getter
public enum ServiceRequestStatus {
    OPEN("Open", "The request is created and waiting for action."),
    ASSIGNED("Assigned", "The request has been assigned to a staff member for action."),
    IN_PROGRESS("Accept", "The request is being worked on by staff."),
    ESCALATED("Escalated", "The request has been escalated to higher-level for attention."),
    REJECTED("Reject", "The request has been reviewed and rejected."),
    CANCELLED("Cancel", "The request has been canceled and will not be processed."),
    REASSIGNED("Reassigned", "The request has been reassigned to a different staff member."),
    COMPLETED("Completed", "The request is finished and resolved."),
    CLOSED("Closed", "The request is closed; no further action will be taken.");

    private final String code;
    private final String description;

    ServiceRequestStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Optional: Get enum by code
    public static ServiceRequestStatus fromCode(String code) {
        for (ServiceRequestStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}

