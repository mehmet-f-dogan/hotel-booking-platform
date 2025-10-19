package dev.mehmetfd.reservation.service;

import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.common.dto.ReservationEvent;
import dev.mehmetfd.common.dto.RoomDto;
import dev.mehmetfd.common.exception.BadRequestException;
import dev.mehmetfd.common.exception.ResourceNotFoundException;
import dev.mehmetfd.common.exception.TryLaterException;
import dev.mehmetfd.common.exception.UnauthorizedException;
import dev.mehmetfd.reservation.model.Reservation;
import dev.mehmetfd.reservation.repository.ReservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final LockService lockService;
    private final ReservationRepository reservationRepository;
    private final ReservationEventProducer reservationEventProducer;
    private final RestTemplate restTemplate;

    public ReservationService(LockService lockService,
            ReservationRepository reservationRepository,
            ReservationEventProducer reservationEventProducer,
            RestTemplate restTemplate) {
        this.lockService = lockService;
        this.reservationRepository = reservationRepository;
        this.reservationEventProducer = reservationEventProducer;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public Reservation createReservation(Long hotelId, Long roomId, String guestName, LocalDate checkIn,
            LocalDate checkOut) {

        String url = "http://HOTEL-SERVICE/rooms/" + roomId;
        RoomDto roomDto;
        try {
            roomDto = restTemplate.getForObject(url, RoomDto.class);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Room not found");
        }

        if (roomDto.hotelId() != hotelId) {
            throw new ResourceNotFoundException("Room not found");
        }

        validateDates(checkIn, checkOut);

        String lockKey = "room-" + roomId;
        final Reservation[] savedReservation = new Reservation[1];

        lockService.executeWithLock(lockKey, () -> {
            List<Reservation> conflicts = reservationRepository.findOverlappingReservations(roomId, checkIn, checkOut);
            if (!conflicts.isEmpty()) {
                throw new BadRequestException("Room " + roomId + " is already reserved in that date range");
            }

            RequestContext ctx = RequestContextHolder.get();
            String accountUsername = ctx.username();

            Reservation reservation = new Reservation(hotelId, roomId, guestName, accountUsername, checkIn, checkOut);
            savedReservation[0] = reservationRepository.save(reservation);

            reservationEventProducer.sendReservationEvent(toEvent(savedReservation[0], accountUsername));
        });

        Reservation returnReservation = savedReservation[0];
        if (returnReservation == null) {
            throw new TryLaterException();
        }
        return returnReservation;
    }

    @Transactional
    public Reservation updateReservation(Long id, LocalDate newCheckIn, LocalDate newCheckOut, String newGuestName) {
        validateDates(newCheckIn, newCheckOut);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        final RequestContext ctx = RequestContextHolder.get();
        final String accountUsername = ctx.username();

        if (!reservation.getAccountUsername().equals(accountUsername)) {
            throw new UnauthorizedException();
        }

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

            reservationEventProducer.sendReservationEvent(toEvent(reservation, accountUsername));
        });

        return reservation;
    }

    public void deleteReservation(long id) {
        Optional<Reservation> reservationOptional = reservationRepository.findById(id);

        if (reservationOptional.isEmpty()) {
            throw new UnauthorizedException();
        }

        Reservation reservation = reservationOptional.get();

        String lockKey = "room-" + reservation.getRoomId();

        RequestContext ctx = RequestContextHolder.get();
        String accountUsername = ctx.username();

        if (!reservation.getAccountUsername().equals(accountUsername)) {
            throw new UnauthorizedException();
        }

        lockService.executeWithLock(lockKey, () -> {
            reservationRepository.deleteById(id);
            reservationEventProducer.sendReservationDeleteEvent(reservation.getId());
        });
    }

    public Optional<Reservation> getReservation(Long id) {
        RequestContext ctx = RequestContextHolder.get();
        String accountUsername = ctx.username();
        Optional<Reservation> optionalReservation = reservationRepository.findById(id);
        if (optionalReservation.isPresent()) {
            Reservation r = optionalReservation.get();
            if (r.getAccountUsername().equals(accountUsername)) {
                return Optional.of(r);
            }
        }
        throw new ResourceNotFoundException("Reservation not found");
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
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

    private ReservationEvent toEvent(Reservation reservation, String accountUsername) {
        return new ReservationEvent(
                reservation.getId(),
                reservation.getHotelId(),
                reservation.getRoomId(),
                reservation.getGuestName(),
                accountUsername,
                reservation.getCheckInDate(),
                reservation.getCheckOutDate());
    }
}
