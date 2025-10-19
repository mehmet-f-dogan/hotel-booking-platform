package dev.mehmetfd.hotel.service;

import dev.mehmetfd.common.constants.Role;
import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.common.exception.ResourceNotFoundException;
import dev.mehmetfd.hotel.dto.HotelRequest;
import dev.mehmetfd.hotel.model.Hotel;
import dev.mehmetfd.hotel.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.set(new RequestContext("id", "testUser", Role.ADMIN));
    }

    @Test
    void testCreateHotel() {
        HotelRequest request = new HotelRequest("Test Hotel", "Address 1", 5);
        Hotel savedHotel = new Hotel();
        savedHotel.setId(1L);
        savedHotel.setName(request.name());
        savedHotel.setAddress(request.address());
        savedHotel.setStarRating(request.starRating());
        savedHotel.setCreatedBy("testUser");

        when(hotelRepository.save(any())).thenReturn(savedHotel);

        Hotel result = hotelService.createHotel(request);

        assertEquals(savedHotel.getId(), result.getId());
        assertEquals("testUser", result.getCreatedBy());
        verify(hotelRepository, times(1)).save(any());
    }

    @Test
    void testGetHotel_Success() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        Hotel result = hotelService.getHotel(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testGetHotel_NotFound() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> hotelService.getHotel(1L));
    }

    @Test
    void testGetAllHotels() {
        when(hotelRepository.findAll()).thenReturn(List.of(new Hotel(), new Hotel()));

        List<Hotel> hotels = hotelService.getAllHotels();

        assertEquals(2, hotels.size());
    }

    @Test
    void testUpdateHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        HotelRequest request = new HotelRequest("Updated Hotel", "Updated Address", 4);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(hotel)).thenReturn(hotel);

        Hotel updated = hotelService.updateHotel(1L, request);

        assertEquals("Updated Hotel", updated.getName());
        assertEquals("Updated Address", updated.getAddress());
        assertEquals(4, updated.getStarRating());
    }

    @Test
    void testDeleteHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        hotelService.deleteHotel(1L);

        verify(hotelRepository, times(1)).delete(hotel);
    }
}
