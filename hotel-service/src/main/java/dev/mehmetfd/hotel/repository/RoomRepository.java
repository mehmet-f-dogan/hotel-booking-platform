package dev.mehmetfd.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.mehmetfd.hotel.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByHotelIdAndRoomNumber(Long hotelId, String roomNumber);

    void deleteAllByHotelId(Long hotelId);

    List<Room> findAllByHotelId(Long hotelId);
}