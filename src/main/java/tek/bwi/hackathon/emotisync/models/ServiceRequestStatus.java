package tek.bwi.hackathon.emotisync.models;

import lombok.Getter;

@Getter
public enum ServiceRequestStatus {
    OPEN("Open", "The request is created and waiting for action."),
    IN_PROGRESS("In_Progress", "The request is being worked on by staff."),
    COMPLETED("Completed", "The request is finished and resolved."),
    CLOSED("Closed", "The request is closed; no further action will be taken."),
    ESCALATED("Escalated", "The request has been escalated to higher-level for attention."),
    ASSIGNED("Assigned", "The request has been assigned to a staff member for action.");

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

