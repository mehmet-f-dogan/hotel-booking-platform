package dev.mehmetfd.reservation.service;

import dev.mehmetfd.common.dto.ReservationEvent;
import dev.mehmetfd.reservation.model.Reservation;
import dev.mehmetfd.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final LockService lockService;
    private final ReservationRepository reservationRepository;
    private final ReservationEventProducer reservationEventProducer;

    public ReservationService(LockService lockService,
            ReservationRepository reservationRepository,
            ReservationEventProducer reservationEventProducer) {
        this.lockService = lockService;
        this.reservationRepository = reservationRepository;
        this.reservationEventProducer = reservationEventProducer;
    }

    @Transactional
    public Reservation createReservation(Long hotelId, Long roomId, String guestName, LocalDate checkIn,
            LocalDate checkOut) {
        validateDates(checkIn, checkOut);

        String lockKey = "room-" + roomId;
        final Reservation[] savedReservation = new Reservation[1];

        lockService.executeWithLock(lockKey, () -> {
            List<Reservation> conflicts = reservationRepository.findOverlappingReservations(roomId, checkIn, checkOut);
            if (!conflicts.isEmpty()) {
                throw new IllegalStateException("Room " + roomId + " is already reserved in that date range");
            }

            Reservation reservation = new Reservation(hotelId, roomId, guestName, checkIn, checkOut);
            savedReservation[0] = reservationRepository.save(reservation);

            reservationEventProducer.sendReservationEvent(
                    new ReservationEvent(hotelId, reservation.getId(), roomId, guestName, checkIn, checkOut));
        });

        return savedReservation[0];
    }

    public Optional<Reservation> getReservation(Long id) {
        return reservationRepository.findById(id);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByRoom(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    public List<Reservation> getReservationsByHotel(Long hotelId) {
        return reservationRepository.findByHotelId(hotelId);
    }

    @Transactional
    public Reservation updateReservation(Long id, LocalDate newCheckIn, LocalDate newCheckOut, String newGuestName) {
        validateDates(newCheckIn, newCheckOut);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        String lockKey = "room-" + reservation.getRoomId();
        lockService.executeWithLock(lockKey, () -> {
            List<Reservation> conflicts = reservationRepository.findOverlappingReservations(
                    reservation.getRoomId(), newCheckIn, newCheckOut);
            conflicts.removeIf(r -> r.getId().equals(id));
            if (!conflicts.isEmpty()) {
                throw new IllegalStateException(
                        "Room " + reservation.getRoomId() + " is already reserved in that date range");
            }

            reservation.setCheckInDate(newCheckIn);
            reservation.setCheckOutDate(newCheckOut);
            reservation.setGuestName(newGuestName);
            reservationRepository.save(reservation);
        });

        return reservation;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        String lockKey = "room-" + reservation.getRoomId();
        lockService.executeWithLock(lockKey, () -> {
            reservationRepository.deleteById(id);
        });
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = LocalDate.now();
        if (checkIn.isBefore(today) || checkOut.isBefore(today)) {
            throw new IllegalArgumentException("Reservation dates cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
    }
}
