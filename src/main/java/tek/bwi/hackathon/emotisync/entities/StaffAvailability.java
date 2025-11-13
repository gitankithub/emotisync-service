package tek.bwi.hackathon.emotisync.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffAvailability {
    private String id;
    private String staffId;
    private String status;
    private String currentRequestId;
    private Instant startTime;
    private Instant endTime;
}

