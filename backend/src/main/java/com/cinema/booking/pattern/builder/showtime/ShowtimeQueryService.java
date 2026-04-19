package com.cinema.booking.pattern.builder.showtime;

import com.cinema.booking.dto.ShowtimeDTO;
import com.cinema.booking.entity.Showtime;
import com.cinema.booking.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  — Sử dụng ShowtimeFilter (Product) để truy vấn và lọc suất chiếu từ DB.
 * Nhận filter đã build từ Builder, áp dụng từng tiêu chí lên stream kết quả.
 */
@Service
public class ShowtimeQueryService {

    private final ShowtimeRepository showtimeRepository;

    public ShowtimeQueryService(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    /**
     * Tìm suất chiếu theo filter đã xây dựng bởi ShowtimeFilterBuilder.
     * @param filter ShowtimeFilter object (immutable, từ Builder)
     * @return Danh sách ShowtimeDTO đã lọc
     */
    public List<ShowtimeDTO> findShowtimes(ShowtimeFilter filter) {
        // Lấy tất cả showtimes từ DB, sau đó filter bằng stream
        Stream<Showtime> stream = showtimeRepository.findAll().stream();

        // Lọc theo cinemaId (qua Room → Cinema)
        if (filter.getCinemaId() != null) {
            stream = stream.filter(s -> s.getRoom() != null
                    && s.getRoom().getCinema() != null
                    && filter.getCinemaId().equals(s.getRoom().getCinema().getCinemaId()));
        }

        // Lọc theo movieId
        if (filter.getMovieId() != null) {
            stream = stream.filter(s -> s.getMovie() != null
                    && filter.getMovieId().equals(s.getMovie().getMovieId()));
        }

        // Lọc theo locationId (Cinema → Location)
        if (filter.getLocationId() != null) {
            stream = stream.filter(s -> s.getRoom() != null
                    && s.getRoom().getCinema() != null
                    && s.getRoom().getCinema().getLocation() != null
                    && filter.getLocationId().equals(s.getRoom().getCinema().getLocation().getId()));
        }

        // Lọc theo ngày chiếu
        if (filter.getDate() != null) {
            stream = stream.filter(s -> s.getStartTime() != null
                    && s.getStartTime().toLocalDate().equals(filter.getDate()));
        }

        // Lọc theo loại màn hình (2D, 3D, IMAX)
        if (filter.getScreenType() != null) {
            stream = stream.filter(s -> s.getRoom() != null
                    && filter.getScreenType().equalsIgnoreCase(s.getRoom().getScreenType()));
        }

        // Lọc theo khoảng giá
        if (filter.getMinPrice() != null) {
            stream = stream.filter(s -> s.getBasePrice() != null
                    && s.getBasePrice().compareTo(filter.getMinPrice()) >= 0);
        }
        if (filter.getMaxPrice() != null) {
            stream = stream.filter(s -> s.getBasePrice() != null
                    && s.getBasePrice().compareTo(filter.getMaxPrice()) <= 0);
        }

        return stream.map(this::mapToDTO).collect(Collectors.toList());
    }

    private ShowtimeDTO mapToDTO(Showtime showtime) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setShowtimeId(showtime.getShowtimeId());
        dto.setMovieId(showtime.getMovie().getMovieId());
        dto.setRoomId(showtime.getRoom().getRoomId());
        dto.setStartTime(showtime.getStartTime());
        dto.setEndTime(showtime.getEndTime());
        dto.setBasePrice(showtime.getBasePrice());
        return dto;
    }
}
