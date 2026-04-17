package com.cinema.booking.controllers;

import com.cinema.booking.dtos.CinemaDTO;
import com.cinema.booking.dtos.FnbItemDTO;
import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.dtos.ShowtimeDTO;
import com.cinema.booking.dtos.LocationDTO;
import com.cinema.booking.entities.FnbCategory;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.repositories.FnbCategoryRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.services.CinemaService;
import com.cinema.booking.services.FnbItemInventoryService;
import com.cinema.booking.services.LocationService;
import com.cinema.booking.services.MovieService;
import com.cinema.booking.services.builder.filter.ShowtimeFilter;
import com.cinema.booking.services.builder.filter.ShowtimeFilterBuilder;
import com.cinema.booking.services.builder.filter.ShowtimeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    private FnbCategoryRepository fnbCategoryRepository;

    @Autowired
    private FnbItemRepository fnbItemRepository;

    @Autowired
    private FnbItemInventoryService fnbItemInventoryService;

    // ──────────────────────────────────────────────────────────────────
    //  Builder Pattern: ShowtimeQueryService sử dụng ShowtimeFilter
    // ──────────────────────────────────────────────────────────────────
    @Autowired
    private ShowtimeQueryService showtimeQueryService;

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

    // 4.6 Menu F&B công khai (danh mục)
    @Operation(summary = "Lấy danh mục F&B", description = "Trả về danh sách danh mục bắp nước cho khách hàng")
    @GetMapping("/fnb/categories")
    public ResponseEntity<List<FnbCategory>> getPublicFnbCategories() {
        return ResponseEntity.ok(fnbCategoryRepository.findAll());
    }

    // 4.7 Menu F&B công khai (sản phẩm) — theo chi nhánh (cinemaId)
    @Operation(summary = "Lấy sản phẩm F&B", description = "Lọc theo cinemaId (rạp). Chỉ trả món đang active.")
    @GetMapping("/fnb/items")
    public ResponseEntity<List<FnbItemDTO>> getPublicFnbItems(@RequestParam(required = false) Integer cinemaId) {
        List<FnbItem> items = cinemaId != null
                ? fnbItemRepository.findByCinema_CinemaId(cinemaId)
                : fnbItemRepository.findAll();
        items = items.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .toList();
        Map<Integer, Integer> quantityMap = fnbItemInventoryService.getQuantityMap(
                items.stream().map(FnbItem::getItemId).toList()
        );
        List<FnbItemDTO> response = items.stream().map(item -> {
            FnbItemDTO dto = new FnbItemDTO();
            dto.setItemId(item.getItemId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setPrice(item.getPrice());
            dto.setStockQuantity(quantityMap.getOrDefault(item.getItemId(), 0));
            dto.setIsActive(item.getIsActive());
            dto.setImageUrl(item.getImageUrl());
            dto.setCategoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null);
            dto.setCategoryName(item.getCategory() != null ? item.getCategory().getName() : null);
            if (item.getCinema() != null) {
                dto.setCinemaId(item.getCinema().getCinemaId());
                dto.setCinemaName(item.getCinema().getName());
            }
            return dto;
        }).toList();
        return ResponseEntity.ok(response);
    }
}
