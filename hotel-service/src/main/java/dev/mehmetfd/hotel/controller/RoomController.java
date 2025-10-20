package dev.mehmetfd.hotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import dev.mehmetfd.common.dto.RoomDto;
import dev.mehmetfd.hotel.dto.RoomRequest;
import dev.mehmetfd.hotel.model.Room;
import dev.mehmetfd.hotel.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room createRoom(@Valid @NotNull @RequestBody RoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping("/{id}")
    public RoomDto getRoom(@PathVariable(name = "id") long id) {
        Room room = roomService.getRoom(id);
        return new RoomDto(id, room.getHotelId(), room.getRoomNumber(), room.getCapacity(), room.getPricePerNight(),
                room.getCreatedBy(), room.getCreatedAt(), room.getUpdatedAt());
    }

    @GetMapping
    public List<RoomDto> getAllRooms() {
        return roomService.getAllRooms().stream().map(room -> {
            return new RoomDto(room.getId(), room.getHotelId(), room.getRoomNumber(), room.getCapacity(),
                    room.getPricePerNight(),
                    room.getCreatedBy(), room.getCreatedAt(), room.getUpdatedAt());
        }).toList();
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable(name = "id") long id, @Valid @NotNull @RequestBody RoomRequest request) {
        return roomService.updateRoom(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable(name = "id") long id) {
        roomService.deleteRoom(id);
    }
}
