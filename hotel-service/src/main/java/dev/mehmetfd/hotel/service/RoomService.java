package dev.mehmetfd.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.mehmetfd.common.context.RequestContext;
import dev.mehmetfd.common.context.RequestContextHolder;
import dev.mehmetfd.common.dto.RoomRemovalEvent;
import dev.mehmetfd.common.exception.BadRequestException;
import dev.mehmetfd.common.exception.ResourceNotFoundException;
import dev.mehmetfd.hotel.dto.RoomRequest;
import dev.mehmetfd.hotel.model.Room;
import dev.mehmetfd.hotel.repository.HotelRepository;
import dev.mehmetfd.hotel.repository.RoomRepository;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomRemovalEventProducer roomRemovalEventProducer;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository,
            RoomRemovalEventProducer roomRemovalEventProducer) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.roomRemovalEventProducer = roomRemovalEventProducer;
    }

    public Room createRoom(RoomRequest request) {
        if (!hotelRepository.existsById(request.hotelId())) {
            throw new ResourceNotFoundException("Hotel not found with id " + request.hotelId());
        }

        if (roomRepository.existsByHotelIdAndRoomNumber(request.hotelId(), request.roomNumber())) {
            throw new BadRequestException("Room number already exists for this hotel");
        }

        RequestContext ctx = RequestContextHolder.get();
        String accountUsername = ctx.username();

        Room room = new Room();
        room.setHotelId(request.hotelId());
        room.setRoomNumber(request.roomNumber());
        room.setCapacity(request.capacity());
        room.setPricePerNight(request.pricePerNight());
        room.setCreatedBy(accountUsername);
        return roomRepository.save(room);
    }

    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room updateRoom(Long id, RoomRequest request) {
        Room room = getRoom(id);

        if (!hotelRepository.existsById(request.hotelId())) {
            throw new ResourceNotFoundException("Hotel not found with id " + request.hotelId());
        }

        room.setCapacity(request.capacity());
        room.setPricePerNight(request.pricePerNight());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        Room room = getRoom(id);
        roomRepository.delete(room);
        roomRemovalEventProducer.sendRoomRemovalEvent(new RoomRemovalEvent(room.getHotelId()));
    }

    public void deleteHotelRooms(Long hotelId) {
        List<Room> removedRooms = roomRepository.findAllByHotelId(hotelId);
        roomRepository.deleteAllByHotelId(hotelId);
        removedRooms.forEach(r -> roomRemovalEventProducer.sendRoomRemovalEvent(new RoomRemovalEvent(r.getHotelId())));
    }
}
