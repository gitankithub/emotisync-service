package tek.bwi.hackathon.emotisync.models;

import lombok.Getter;

@Getter
public enum UserRole {
    GUEST("GUEST"),
    STAFF("STAFF"),
    ASSISTANT("ASSISTANT"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}

