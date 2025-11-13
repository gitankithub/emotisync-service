package tek.bwi.hackathon.emotisync.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "reservations")
public class Reservation {
    @Id
    private String id;
    private String name;
    private String roomNumber;
    private String roomType;         // e.g., DELUXE, SUITE, STANDARD
    private int numberOfOccupants;   // e.g., 2
    private String guestId;           // Who made the reservation
    private String status;           // BOOKED, CHECKED_IN, CANCELLED, COMPLETED
    private String checkInDate;      // ISO date string
    private String checkOutDate;
    private String createdAt;
    private String updatedAt;
}

