package dev.mehmetfd.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mehmetfd.hotel.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByHotelIdAndRoomNumber(Long hotelId, String roomNumber);
}