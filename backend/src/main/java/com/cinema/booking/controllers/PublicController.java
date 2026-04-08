package com.cinema.booking.controllers;

import com.cinema.booking.dtos.CinemaDTO;
import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.dtos.ShowtimeDTO;
import com.cinema.booking.dtos.LocationDTO;
import com.cinema.booking.entities.FnbCategory;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.repositories.FnbCategoryRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.services.CinemaService;
import com.cinema.booking.services.LocationService;
import com.cinema.booking.services.MovieService;
import com.cinema.booking.services.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    private ShowtimeService showtimeService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private FnbCategoryRepository fnbCategoryRepository;

    @Autowired
    private FnbItemRepository fnbItemRepository;

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

    // 4.5 Lịch chiếu công khai (có filter)
    @Operation(summary = "Lấy lịch chiếu công khai", description = "Trả về danh sách lịch chiếu, có thể lọc theo cinemaId, movieId, date. Trả kèm thông tin phim, phòng, rạp.")
    @GetMapping("/showtimes")
    public ResponseEntity<List<ShowtimeDTO>> getPublicShowtimes(
            @RequestParam(required = false) Integer cinemaId,
            @RequestParam(required = false) Integer movieId,
            @RequestParam(required = false) String date) {

        List<ShowtimeDTO> all = showtimeService.getAllShowtimes();

        // Filter theo cinemaId (enriched field trên ShowtimeDTO)
        if (cinemaId != null) {
            all = all.stream().filter(s -> cinemaId.equals(s.getCinemaId())).collect(Collectors.toList());
        }
        // Filter theo movieId
        if (movieId != null) {
            all = all.stream().filter(s -> movieId.equals(s.getMovieId())).collect(Collectors.toList());
        }
        // Filter theo date (YYYY-MM-DD)
        if (date != null && !date.isBlank()) {
            LocalDate filterDate = LocalDate.parse(date);
            all = all.stream().filter(s -> s.getStartTime().toLocalDate().equals(filterDate)).collect(Collectors.toList());
        }
        return ResponseEntity.ok(all);
    }

    // 4.6 Menu F&B công khai (danh mục)
    @Operation(summary = "Lấy danh mục F&B", description = "Trả về danh sách danh mục bắp nước cho khách hàng")
    @GetMapping("/fnb/categories")
    public ResponseEntity<List<FnbCategory>> getPublicFnbCategories() {
        return ResponseEntity.ok(fnbCategoryRepository.findAll());
    }

    // 4.7 Menu F&B công khai (sản phẩm)
    @Operation(summary = "Lấy sản phẩm F&B", description = "Trả về danh sách sản phẩm bắp nước kèm giá cho khách hàng")
    @GetMapping("/fnb/items")
    public ResponseEntity<List<FnbItem>> getPublicFnbItems() {
        return ResponseEntity.ok(fnbItemRepository.findAll());
    }
}

