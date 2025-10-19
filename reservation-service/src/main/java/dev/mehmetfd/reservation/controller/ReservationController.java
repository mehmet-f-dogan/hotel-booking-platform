package dev.mehmetfd.reservation.controller;

import dev.mehmetfd.common.dto.CreateReservationRequest;
import dev.mehmetfd.common.dto.UpdateReservationRequest;
import dev.mehmetfd.reservation.model.Reservation;
import dev.mehmetfd.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/create")
    public Reservation create(@RequestBody @Valid CreateReservationRequest request) {
        return reservationService.createReservation(
                request.hotelId(),
                request.roomId(),
                request.guestName(),
                request.checkIn(),
                request.checkOut());
    }

    @GetMapping("/{id}")
    public Optional<Reservation> get(@PathVariable Long id) {
        return reservationService.getReservation(id);
    }

    @GetMapping("/all")
    public List<Reservation> getAll() {
        return reservationService.getAllReservations();
    }

    @PutMapping("/{id}")
    public Reservation update(@PathVariable Long id, @RequestBody @Valid UpdateReservationRequest request) {
        return reservationService.updateReservation(
                id,
                request.checkIn(),
                request.checkOut(),
                request.guestName());
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return "Deleted successfully";
    }
}
