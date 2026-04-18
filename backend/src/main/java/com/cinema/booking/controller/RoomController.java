package com.cinema.booking.controller;

import com.cinema.booking.dto.RoomDTO;
import com.cinema.booking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // GET /api/rooms?cinemaId=1
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms(
            @RequestParam(required = false) Integer cinemaId) {
        if (cinemaId != null) {
            return ResponseEntity.ok(roomService.getRoomsByCinema(cinemaId));
        }
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Integer id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.createRoom(roomDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Integer id, @Valid @RequestBody RoomDTO roomDTO) {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }
}
