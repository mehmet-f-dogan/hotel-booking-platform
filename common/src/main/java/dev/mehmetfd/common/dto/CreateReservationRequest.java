package dev.mehmetfd.common.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
                @NotNull Long hotelId,
                @NotNull Long roomId,
                @NotBlank String guestName,
                @NotNull @FutureOrPresent LocalDate checkIn,
                @NotNull @FutureOrPresent LocalDate checkOut) {
}
