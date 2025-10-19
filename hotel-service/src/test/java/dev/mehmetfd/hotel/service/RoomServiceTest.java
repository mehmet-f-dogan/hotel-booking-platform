package dev.mehmetfd.hotel.service;

import dev.mehmetfd.common.constants.Role;
import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.common.exception.BadRequestException;
import dev.mehmetfd.common.exception.ResourceNotFoundException;
import dev.mehmetfd.hotel.dto.RoomRequest;
import dev.mehmetfd.hotel.model.Room;
import dev.mehmetfd.hotel.repository.HotelRepository;
import dev.mehmetfd.hotel.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.set(new RequestContext("id", "testUser", Role.ADMIN));
    }

    @Test
    void testCreateRoom_Success() {
        RoomRequest request = new RoomRequest(1L, "101", 2, BigDecimal.TEN);

        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(false);

        Room room = new Room();
        room.setId(1L);
        room.setHotelId(1L);
        room.setRoomNumber("101");
        room.setCapacity(2);
        room.setPricePerNight(BigDecimal.TEN);
        room.setCreatedBy("testUser");

        when(roomRepository.save(any())).thenReturn(room);

        Room result = roomService.createRoom(request);

        assertEquals(1L, result.getId());
        assertEquals("testUser", result.getCreatedBy());
    }

    @Test
    void testCreateRoom_HotelNotFound() {
        RoomRequest request = new RoomRequest(1L, "101", 2, BigDecimal.TEN);
        when(hotelRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> roomService.createRoom(request));
    }

    @Test
    void testCreateRoom_RoomNumberExists() {
        RoomRequest request = new RoomRequest(1L, "101", 2, BigDecimal.TEN);
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> roomService.createRoom(request));
    }

    @Test
    void testGetRoom_NotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoom(1L));
    }

    @Test
    void testUpdateRoom_Success() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("102");

        RoomRequest request = new RoomRequest(1L, "102", 3, BigDecimal.TEN);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.save(room)).thenReturn(room);

        Room updated = roomService.updateRoom(1L, request);

        assertEquals("102", updated.getRoomNumber());
        assertEquals(3, updated.getCapacity());
        assertEquals(BigDecimal.TEN, updated.getPricePerNight());
    }
}
