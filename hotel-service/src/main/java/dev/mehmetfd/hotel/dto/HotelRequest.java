package dev.mehmetfd.hotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record HotelRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Address is required") String address,
        @Min(value = 1, message = "Star rating must be at least 1") @Max(value = 5, message = "Star rating cannot exceed 5") int starRating) {
}
