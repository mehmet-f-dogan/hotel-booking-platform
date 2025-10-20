package dev.mehmetfd.common.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReservationRequest(
                @NotNull @FutureOrPresent LocalDate checkIn,
                @NotNull @FutureOrPresent LocalDate checkOut,
                @NotBlank String guestName) {
}
