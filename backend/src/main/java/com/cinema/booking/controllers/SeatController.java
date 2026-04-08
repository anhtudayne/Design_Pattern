package com.cinema.booking.controllers;

import com.cinema.booking.dtos.SeatDTO;
import com.cinema.booking.services.SeatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    // LẤY DS SƠ ĐỒ GHẾ CỦA PHÒNG: GET /api/seats?roomId=1
    @GetMapping
    public ResponseEntity<List<SeatDTO>> getAllSeats(
            @RequestParam(required = false) Integer roomId) {
        if (roomId != null) {
            return ResponseEntity.ok(seatService.getSeatsByRoom(roomId));
        }
        return ResponseEntity.ok(seatService.getAllSeats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatDTO> getSeatById(@PathVariable Integer id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @PostMapping
    public ResponseEntity<SeatDTO> createSeat(@Valid @RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(seatService.createSeat(seatDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatDTO> updateSeat(@PathVariable Integer id, @Valid @RequestBody SeatDTO seatDTO) {
        return ResponseEntity.ok(seatService.updateSeat(id, seatDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeat(@PathVariable Integer id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok().build();
    }

    // BATCH: Thay thế toàn bộ ghế trong 1 phòng (1 request thay vì N request)
    @PutMapping("/batch/{roomId}")
    public ResponseEntity<List<SeatDTO>> replaceAllSeats(
            @PathVariable Integer roomId,
            @RequestBody List<SeatDTO> seatDTOs) {
        return ResponseEntity.ok(seatService.replaceAllSeatsInRoom(roomId, seatDTOs));
    }
}

