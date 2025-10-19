package dev.mehmetfd.hotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(
        @NotNull(message = "Hotel ID is required") Long hotelId,
        @NotBlank(message = "Room number is required") String roomNumber,
        @Min(value = 1, message = "Capacity must be at least 1") int capacity,
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal pricePerNight) {
}
