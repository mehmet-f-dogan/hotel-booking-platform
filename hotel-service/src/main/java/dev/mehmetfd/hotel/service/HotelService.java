package dev.mehmetfd.hotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.common.exception.ResourceNotFoundException;
import dev.mehmetfd.hotel.dto.HotelRequest;
import dev.mehmetfd.hotel.model.Hotel;
import dev.mehmetfd.hotel.repository.HotelRepository;

import java.util.List;

@Service
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomService roomService;

    public HotelService(HotelRepository hotelRepository, RoomService roomService) {
        this.hotelRepository = hotelRepository;
        this.roomService = roomService;
    }

    public Hotel createHotel(HotelRequest request) {
        RequestContext ctx = RequestContextHolder.get();
        String accountUsername = ctx.username();

        Hotel hotel = new Hotel();
        hotel.setName(request.name());
        hotel.setAddress(request.address());
        hotel.setStarRating(request.starRating());
        hotel.setCreatedBy(accountUsername);
        return hotelRepository.save(hotel);
    }

    public Hotel getHotel(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id " + id));
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel updateHotel(Long id, HotelRequest request) {
        Hotel hotel = getHotel(id);
        hotel.setName(request.name());
        hotel.setAddress(request.address());
        hotel.setStarRating(request.starRating());
        return hotelRepository.save(hotel);
    }

    @Transactional
    public void deleteHotel(Long id) {
        Hotel hotel = getHotel(id);
        hotelRepository.delete(hotel);
        roomService.deleteHotelRooms(id);
    }
}
