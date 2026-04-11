package com.cinema.booking.services;

import com.cinema.booking.dtos.ShowtimeDTO;
import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeDTO> getAllShowtimes();
    ShowtimeDTO getShowtimeById(Integer id);
    ShowtimeDTO createShowtime(ShowtimeDTO showtimeDTO);
    ShowtimeDTO updateShowtime(Integer id, ShowtimeDTO showtimeDTO);
    void deleteShowtime(Integer id);
    List<ShowtimeDTO> searchPublicShowtimes(Integer cinemaId, Integer movieId, LocalDate date);
}
