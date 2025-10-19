package dev.mehmetfd.hotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import dev.mehmetfd.hotel.dto.HotelRequest;
import dev.mehmetfd.hotel.model.Hotel;
import dev.mehmetfd.hotel.service.HotelService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hotel createHotel(@Valid @RequestBody HotelRequest request) {
        return hotelService.createHotel(request);
    }

    @GetMapping("/{id}")
    public Hotel getHotel(@PathVariable Long id) {
        return hotelService.getHotel(id);
    }

    @GetMapping
    public List<Hotel> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @PutMapping("/{id}")
    public Hotel updateHotel(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return hotelService.updateHotel(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}
