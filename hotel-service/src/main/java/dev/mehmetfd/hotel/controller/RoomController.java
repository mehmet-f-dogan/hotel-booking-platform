package dev.mehmetfd.hotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import dev.mehmetfd.hotel.dto.RoomRequest;
import dev.mehmetfd.hotel.model.Room;
import dev.mehmetfd.hotel.service.RoomService;
import jakarta.validation.Valid;
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
    public Room createRoom(@Valid @RequestBody RoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping("/{id}")
    public Room getRoom(@PathVariable Long id) {
        return roomService.getRoom(id);
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.updateRoom(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }
}
