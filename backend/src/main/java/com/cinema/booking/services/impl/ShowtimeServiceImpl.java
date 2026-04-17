package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.ShowtimeDTO;
import com.cinema.booking.entities.Movie;
import com.cinema.booking.entities.Room;
import com.cinema.booking.entities.Showtime;
import com.cinema.booking.repositories.MovieRepository;
import com.cinema.booking.repositories.RoomRepository;
import com.cinema.booking.repositories.ShowtimeRepository;
import com.cinema.booking.services.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    private ShowtimeDTO mapToDTO(Showtime showtime) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setShowtimeId(showtime.getShowtimeId());
        dto.setMovieId(showtime.getMovie().getMovieId());
        dto.setRoomId(showtime.getRoom().getRoomId());
        dto.setStartTime(showtime.getStartTime());
        dto.setEndTime(showtime.getEndTime());
        dto.setBasePrice(showtime.getBasePrice());
        dto.setSurcharge(BigDecimal.ZERO);

        // Enriched fields for frontend display
        Movie movie = showtime.getMovie();
        dto.setMovieTitle(movie.getTitle());
        dto.setMoviePosterUrl(movie.getPosterUrl());
        dto.setMovieDurationMinutes(movie.getDurationMinutes());

        Room room = showtime.getRoom();
        dto.setRoomName(room.getName());
        dto.setScreenType(room.getScreenType());
        dto.setCinemaId(room.getCinema().getCinemaId());
        dto.setCinemaName(room.getCinema().getName());

        return dto;
    }

    @Override
    public List<ShowtimeDTO> getAllShowtimes() {
        return showtimeRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ShowtimeDTO getShowtimeById(Integer id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu này!"));
        return mapToDTO(showtime);
    }

    @Override
    public ShowtimeDTO createShowtime(ShowtimeDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại!"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng chiếu không tồn tại!"));

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(dto.getStartTime());
        
        // Tính EndTime = StartTime + Movie Duration
        showtime.setEndTime(dto.getStartTime().plusMinutes(movie.getDurationMinutes()));
        
        showtime.setBasePrice(dto.getBasePrice());

        return mapToDTO(showtimeRepository.save(showtime));
    }

    @Override
    public ShowtimeDTO updateShowtime(Integer id, ShowtimeDTO dto) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không thể cập nhật: Suất chiếu không tồn tại!"));
        
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại!"));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng chiếu không tồn tại!"));

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(dto.getStartTime());
        showtime.setEndTime(dto.getStartTime().plusMinutes(movie.getDurationMinutes()));
        showtime.setBasePrice(dto.getBasePrice());

        return mapToDTO(showtimeRepository.save(showtime));
    }

    @Override
    public void deleteShowtime(Integer id) {
        showtimeRepository.deleteById(id);
    }

    @Override
    public List<ShowtimeDTO> searchPublicShowtimes(Integer cinemaId, Integer movieId, LocalDate date) {
        // TODO: Re-implement with custom repository query or simple filtering
        // Specification pattern has been removed
        return showtimeRepository.findAll().stream()
                .filter(s -> cinemaId == null || s.getRoom().getCinema().getCinemaId().equals(cinemaId))
                .filter(s -> movieId == null || s.getMovie().getMovieId().equals(movieId))
                .filter(s -> date == null || s.getStartTime().toLocalDate().equals(date))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
