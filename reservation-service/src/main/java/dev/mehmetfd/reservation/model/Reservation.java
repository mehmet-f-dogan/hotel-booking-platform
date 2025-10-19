package dev.mehmetfd.reservation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_room_dates", columnList = "roomId,checkInDate,checkOutDate")
})
@NoArgsConstructor
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String accountUsername;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Reservation(Long hotelId, Long roomId, String guestName, String accountUsername, LocalDate checkInDate,
            LocalDate checkOutDate) {
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.guestName = guestName;
        this.accountUsername = accountUsername;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }
}
