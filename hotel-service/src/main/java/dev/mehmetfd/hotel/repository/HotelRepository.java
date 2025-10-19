package dev.mehmetfd.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mehmetfd.hotel.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
