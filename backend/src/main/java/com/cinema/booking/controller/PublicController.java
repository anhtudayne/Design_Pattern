package com.cinema.booking.controller;

import com.cinema.booking.dto.BookingPageDTO;
import com.cinema.booking.dto.CinemaDTO;
import com.cinema.booking.dto.FnbItemDTO;
import com.cinema.booking.dto.LocationDTO;
import com.cinema.booking.dto.MovieDTO;
import com.cinema.booking.dto.MovieDetailDTO;
import com.cinema.booking.dto.ShowtimeDTO;
import com.cinema.booking.entity.FnbItem;
import com.cinema.booking.entity.Movie.MovieStatus;
import com.cinema.booking.pattern.builder.showtime.ShowtimeFilter;
import com.cinema.booking.pattern.builder.showtime.ShowtimeFilterBuilder;
import com.cinema.booking.pattern.builder.showtime.ShowtimeQueryService;
import com.cinema.booking.pattern.facade.BookingFacade;
import com.cinema.booking.repository.FnbItemRepository;
import com.cinema.booking.service.CinemaService;
import com.cinema.booking.service.LocationService;
import com.cinema.booking.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
@Tag(name = "4. API Mặt tiền (End-User)", description = "Các API công khai dành cho khách hàng xem phim và rạp")
public class PublicController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private FnbItemRepository fnbItemRepository;

    // ──────────────────────────────────────────────────────────────────
    //  Builder Pattern: ShowtimeQueryService sử dụng ShowtimeFilter
    // ──────────────────────────────────────────────────────────────────
    @Autowired
    private ShowtimeQueryService showtimeQueryService;

    // ──────────────────────────────────────────────────────────────────
    //  Facade Pattern: BookingFacade gom gọn nhiều service
    // ──────────────────────────────────────────────────────────────────
    @Autowired
    private BookingFacade bookingFacade;

    // 4.1 API Đổ danh sách "Phim đang chiếu"
    @Operation(summary = "Lấy danh sách phim đang chiếu", description = "Trả về danh sách các bộ phim có trạng thái NOW_SHOWING")
    @GetMapping("/movies/now-showing")
    public ResponseEntity<List<MovieDTO>> getNowShowingMovies() {
        return ResponseEntity.ok(movieService.getMoviesByStatus(MovieStatus.NOW_SHOWING));
    }

    // 4.2 API Đổ danh sách "Phim sắp chiếu"
    @Operation(summary = "Lấy danh sách phim sắp chiếu", description = "Trả về danh sách các bộ phim có trạng thái COMING_SOON")
    @GetMapping("/movies/coming-soon")
    public ResponseEntity<List<MovieDTO>> getComingSoonMovies() {
        return ResponseEntity.ok(movieService.getMoviesByStatus(MovieStatus.COMING_SOON));
    }

    // 4.3 Danh sách cụm rạp để lọc
    @Operation(summary = "Lấy danh sách cụm rạp", description = "Trả về danh sách tất cả các cụm rạp để khách hàng lựa chọn lọc")
    @GetMapping("/cinemas")
    public ResponseEntity<List<CinemaDTO>> getAllCinemas() {
        return ResponseEntity.ok(cinemaService.getAllCinemas());
    }

    // 4.4 Danh sách tỉnh/thành để Customer chọn rạp
    @Operation(summary = "Lấy danh sách tỉnh/thành", description = "Trả về danh sách tất cả tỉnh/thành phố có rạp StarCine")
    @GetMapping("/locations")
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  4.5 Lịch chiếu — Sử dụng Builder Pattern (ShowtimeFilterBuilder)
    //  Giữ nguyên endpoint cũ /showtimes cho backward compatibility
    // ═══════════════════════════════════════════════════════════════════
    @Operation(summary = "Lấy lịch chiếu công khai", description = "Trả về danh sách lịch chiếu, có thể lọc theo cinemaId, movieId, date. Sử dụng Builder Pattern để xây dựng filter.")
    @GetMapping("/showtimes")
    public ResponseEntity<List<ShowtimeDTO>> getPublicShowtimes(
            @RequestParam(required = false) Integer cinemaId,
            @RequestParam(required = false) Integer movieId,
            @RequestParam(required = false) String date) {

        // Sử dụng Builder Pattern thay vì filter thủ công
        ShowtimeFilter filter = new ShowtimeFilterBuilder()
                .byCinema(cinemaId)
                .byMovie(movieId)
                .byDate(date != null && !date.isBlank() ? LocalDate.parse(date) : null)
                .build();

        return ResponseEntity.ok(showtimeQueryService.findShowtimes(filter));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  4.5b Lịch chiếu mở rộng — Builder Pattern với đầy đủ tiêu chí
    //  Endpoint mới /showtimes/filter hỗ trợ thêm locationId, screenType, price range
    // ═══════════════════════════════════════════════════════════════════
    @Operation(summary = "Lọc lịch chiếu nâng cao", description = "Lọc lịch chiếu với nhiều tiêu chí: cinemaId, movieId, date, locationId, screenType, giá. Sử dụng Builder Pattern.")
    @GetMapping("/showtimes/filter")
    public ResponseEntity<List<ShowtimeDTO>> filterShowtimes(
            @RequestParam(required = false) Integer cinemaId,
            @RequestParam(required = false) Integer movieId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer locationId,
            @RequestParam(required = false) String screenType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        ShowtimeFilter filter = new ShowtimeFilterBuilder()
                .byCinema(cinemaId)
                .byMovie(movieId)
                .byDate(date != null && !date.isBlank() ? LocalDate.parse(date) : null)
                .byLocation(locationId)
                .byScreenType(screenType)
                .byPriceRange(minPrice, maxPrice)
                .build();

        return ResponseEntity.ok(showtimeQueryService.findShowtimes(filter));
    }

    @Operation(summary = "Lấy sản phẩm F&B", description = "Danh sách F&B dùng chung toàn hệ thống.")
    @GetMapping("/fnb/items")
    public ResponseEntity<List<FnbItemDTO>> getPublicFnbItems() {
        List<FnbItem> items = fnbItemRepository.findAll();

        List<FnbItemDTO> response = items.stream().map(item -> {
            FnbItemDTO dto = new FnbItemDTO();
            dto.setFnbItemId(item.getFnbItemId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setPrice(item.getPrice());
            dto.setImageUrl(item.getImageUrl());
            dto.setIsActive(item.getIsActive());
            return dto;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  4.7 Facade Pattern — API gom gọn cho trang đặt vé Customer
    //  1 API thay vì 5 API riêng lẻ (movies + cinemas + locations + showtimes + fnb)
    // ═══════════════════════════════════════════════════════════════════
    @Operation(summary = "Lấy toàn bộ dữ liệu trang đặt vé (Facade Pattern)",
            description = "Trả về 1 response gom gọn: phim đang chiếu, danh sách rạp, tỉnh/thành, lịch chiếu (có lọc), F&B. " +
                    "Frontend chỉ cần gọi 1 API thay vì 5 API riêng lẻ.")
    @GetMapping("/booking-page")
    public ResponseEntity<BookingPageDTO> getBookingPageData(
            @RequestParam(required = false) Integer locationId,
            @RequestParam(required = false) Integer cinemaId,
            @RequestParam(required = false) Integer movieId,
            @RequestParam(required = false) String date) {

        LocalDate parsedDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        return ResponseEntity.ok(bookingFacade.getBookingPageData(locationId, cinemaId, movieId, parsedDate));
    }

    @Operation(summary = "Lấy chi tiết phim + lịch chiếu (Facade Pattern)",
            description = "Trả về thông tin chi tiết 1 phim kèm danh sách suất chiếu và rạp có chiếu phim đó.")
    @GetMapping("/movie-detail/{movieId}")
    public ResponseEntity<MovieDetailDTO> getMovieDetailWithShowtimes(@PathVariable Integer movieId) {
        return ResponseEntity.ok(bookingFacade.getMovieDetailWithShowtimes(movieId));
    }
}
