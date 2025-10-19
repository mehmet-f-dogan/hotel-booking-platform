package dev.mehmetfd.reservation.repository;

import dev.mehmetfd.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
                SELECT r FROM Reservation r
                WHERE r.roomId = :roomId
                  AND r.checkInDate < :checkOutDate
                  AND r.checkOutDate > :checkInDate
            """)
    List<Reservation> findOverlappingReservations(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    List<Reservation> findByRoomId(Long roomId);

    List<Reservation> findByHotelId(Long hotelId);
}
