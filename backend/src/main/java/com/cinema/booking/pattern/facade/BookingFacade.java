package com.cinema.booking.pattern.facade;

import com.cinema.booking.dto.*;
import com.cinema.booking.entity.FnbItem;
import com.cinema.booking.entity.Movie.MovieStatus;
import com.cinema.booking.pattern.builder.showtime.ShowtimeFilter;
import com.cinema.booking.pattern.builder.showtime.ShowtimeFilterBuilder;
import com.cinema.booking.pattern.builder.showtime.ShowtimeQueryService;
import com.cinema.booking.repository.FnbItemRepository;
import com.cinema.booking.service.CinemaService;
import com.cinema.booking.service.LocationService;
import com.cinema.booking.service.MovieService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Facade Pattern — Gom gọn nhiều subsystem (MovieService, CinemaService,
 * LocationService, ShowtimeQueryService, FnbItemRepository) thành các method
 * cao cấp đơn giản cho Client (PublicController).
 *
 * Trước Facade: Frontend gọi 5 API riêng lẻ để dựng 1 trang đặt vé.
 * Sau Facade: Frontend chỉ gọi 1 API duy nhất → nhận BookingPageDTO đầy đủ.
 */
@Service
public class BookingFacade {

    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final LocationService locationService;
    private final ShowtimeQueryService showtimeQueryService;
    private final FnbItemRepository fnbItemRepository;

    public BookingFacade(MovieService movieService,
                         CinemaService cinemaService,
                         LocationService locationService,
                         ShowtimeQueryService showtimeQueryService,
                         FnbItemRepository fnbItemRepository) {
        this.movieService = movieService;
        this.cinemaService = cinemaService;
        this.locationService = locationService;
        this.showtimeQueryService = showtimeQueryService;
        this.fnbItemRepository = fnbItemRepository;
    }

    /**
     * Lấy toàn bộ dữ liệu cần thiết cho trang đặt vé Customer.
     * Gom: phim đang chiếu + tất cả rạp + tỉnh/thành + lịch chiếu (có thể lọc) + F&B.
     *
     * @param locationId ID tỉnh/thành (optional — lọc rạp theo khu vực)
     * @param cinemaId   ID rạp (optional — lọc lịch chiếu theo rạp)
     * @param movieId    ID phim (optional — lọc lịch chiếu theo phim)
     * @param date       Ngày chiếu (optional — lọc lịch chiếu theo ngày)
     * @return BookingPageDTO chứa tất cả dữ liệu
     */
    public BookingPageDTO getBookingPageData(Integer locationId, Integer cinemaId,
                                              Integer movieId, LocalDate date) {
        // 1. Lấy danh sách phim đang chiếu
        List<MovieDTO> movies = movieService.getMoviesByStatus(MovieStatus.NOW_SHOWING);

        // 2. Lấy danh sách tất cả cụm rạp
        List<CinemaDTO> cinemas = cinemaService.getAllCinemas();

        // 3. Lấy danh sách tỉnh/thành
        List<LocationDTO> locations = locationService.getAllLocations();

        // 4. Lấy lịch chiếu (sử dụng Builder Pattern để lọc)
        ShowtimeFilter filter = new ShowtimeFilterBuilder()
                .byLocation(locationId)
                .byCinema(cinemaId)
                .byMovie(movieId)
                .byDate(date)
                .build();
        List<ShowtimeDTO> showtimes = showtimeQueryService.findShowtimes(filter);

        // 5. Lấy danh sách F&B đang active
        List<FnbItemDTO> fnbItems = fnbItemRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .map(this::toFnbItemDto)
                .collect(Collectors.toList());

        return BookingPageDTO.builder()
                .movies(movies)
                .cinemas(cinemas)
                .locations(locations)
                .showtimes(showtimes)
                .fnbItems(fnbItems)
                .build();
    }

    /**
     * Lấy chi tiết 1 phim kèm danh sách suất chiếu + rạp có chiếu phim đó.
     * Dùng cho trang chi tiết phim (MovieDetail page).
     *
     * @param movieId ID phim
     * @return MovieDetailDTO chứa thông tin phim + showtimes + cinemas
     */
    public MovieDetailDTO getMovieDetailWithShowtimes(Integer movieId) {
        // 1. Lấy chi tiết phim
        MovieDTO movie = movieService.getMovieById(movieId);

        // 2. Lấy tất cả suất chiếu của phim này
        ShowtimeFilter filter = new ShowtimeFilterBuilder()
                .byMovie(movieId)
                .build();
        List<ShowtimeDTO> showtimes = showtimeQueryService.findShowtimes(filter);

        // 3. Lấy danh sách rạp (để frontend biết rạp nào chiếu phim này)
        List<CinemaDTO> cinemas = cinemaService.getAllCinemas();

        return MovieDetailDTO.builder()
                .movie(movie)
                .showtimes(showtimes)
                .availableCinemas(cinemas)
                .build();
    }

    /**
     * Helper method convert FnbItem entity sang DTO.
     */
    private FnbItemDTO toFnbItemDto(FnbItem item) {
        FnbItemDTO dto = new FnbItemDTO();
        dto.setFnbItemId(item.getFnbItemId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setImageUrl(item.getImageUrl());
        dto.setIsActive(item.getIsActive());
        return dto;
    }
}
