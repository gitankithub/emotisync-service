package tek.bwi.hackathon.emotisync.models;

import lombok.Getter;

@Getter
public enum ActionDetailEnum {
    ESCALATE("ESCALATE"),
    CREATE_SERVICE_REQUEST("CREATE_SERVICE_REQUEST"),
    COMPLETED("COMPLETED"),
    CLOSE_REQUEST("CLOSE_REQUEST");

    private final String action;

    ActionDetailEnum(String action) {
        this.action = action;
    }

    // Optional: For converting string to enum
    public static ActionDetailEnum fromString(String input) {
        for (ActionDetailEnum a : ActionDetailEnum.values()) {
            if (a.getAction().equalsIgnoreCase(input)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown action: " + input);
    }
}

