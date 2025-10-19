package dev.mehmetfd.common.dto;

import java.time.LocalDate;

public record ReservationEvent(
        Long reservationId,
        Long hotelId,
        Long roomId,
        String guestName,
        LocalDate checkInDate,
        LocalDate checkOutDate) {
}