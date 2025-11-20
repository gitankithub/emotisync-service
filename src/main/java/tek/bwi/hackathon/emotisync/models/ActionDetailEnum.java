package tek.bwi.hackathon.emotisync.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Getter;

@Getter
public enum ActionDetailEnum {
    ESCALATE("ESCALATE"),
    CREATE_SERVICE_REQUEST("CREATE_SERVICE_REQUEST"),
    COMPLETED("COMPLETED"),
    UPDATE_SERVICE_REQUEST("UPDATE_SERVICE_REQUEST"),
    UPDATE_REQUEST("UPDATE_REQUEST"),
    CLOSE_REQUEST("CLOSE_REQUEST"),
    @JsonEnumDefaultValue
    OTHER("OTHER");

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

