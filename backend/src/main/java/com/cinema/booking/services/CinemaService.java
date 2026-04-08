package com.cinema.booking.services;

import com.cinema.booking.dtos.CinemaDTO;
import java.util.List;

public interface CinemaService {
    List<CinemaDTO> getAllCinemas();
    List<CinemaDTO> getCinemasByLocation(Integer locationId);
    CinemaDTO getCinemaById(Integer id);
    CinemaDTO createCinema(CinemaDTO cinemaDTO);
    CinemaDTO updateCinema(Integer id, CinemaDTO cinemaDTO);
    void deleteCinema(Integer id);
}
