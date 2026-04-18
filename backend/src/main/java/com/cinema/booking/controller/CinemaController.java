package com.cinema.booking.controller;

import com.cinema.booking.dto.CinemaDTO;
import com.cinema.booking.service.CinemaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    // LẤY DS CỤM RẠP - HỖ TRỢ LỌC THEO locationId = ?
    @GetMapping
    public ResponseEntity<List<CinemaDTO>> getAllCinemas(
            @RequestParam(required = false) Integer locationId) {
        if (locationId != null) {
            return ResponseEntity.ok(cinemaService.getCinemasByLocation(locationId));
        }
        return ResponseEntity.ok(cinemaService.getAllCinemas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaDTO> getCinemaById(@PathVariable Integer id) {
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

    @PostMapping
    public ResponseEntity<CinemaDTO> createCinema(@Valid @RequestBody CinemaDTO cinemaDTO) {
        return ResponseEntity.ok(cinemaService.createCinema(cinemaDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CinemaDTO> updateCinema(@PathVariable Integer id, @Valid @RequestBody CinemaDTO cinemaDTO) {
        return ResponseEntity.ok(cinemaService.updateCinema(id, cinemaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCinema(@PathVariable Integer id) {
        cinemaService.deleteCinema(id);
        return ResponseEntity.ok().build();
    }
}
