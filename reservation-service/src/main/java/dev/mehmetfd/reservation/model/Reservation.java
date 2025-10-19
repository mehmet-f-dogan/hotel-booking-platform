package dev.mehmetfd.reservation.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

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

    @Nonnull
    private Long hotelId;

    @Nonnull
    private Long roomId;

    @Nonnull
    private String guestName;

    @Nonnull
    private String accountUsername;

    @Nonnull
    private LocalDate checkInDate;

    @Nonnull
    private LocalDate checkOutDate;

    @CreationTimestamp
    private LocalDateTime createdAt;

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
