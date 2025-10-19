package dev.mehmetfd.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RoomDto(
        Long id,
        Long hotelId,
        String roomNumber,
        int capacity,
        BigDecimal pricePerNight, String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}