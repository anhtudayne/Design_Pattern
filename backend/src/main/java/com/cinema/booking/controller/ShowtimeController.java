package com.cinema.booking.controller;

import com.cinema.booking.dto.ShowtimeDTO;
import com.cinema.booking.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/showtimes")
@Tag(name = "3. Quản lý Lịch chiếu (Admin)", description = "Các API dành cho Admin để thiết lập lịch chiếu và phụ thu")
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    @Operation(summary = "Lấy danh sách tất cả lịch chiếu", description = "Trả về toàn bộ thông tin lịch chiếu hiện có trong hệ thống")
    @GetMapping
    public ResponseEntity<List<ShowtimeDTO>> getAllShowtimes() {
        return ResponseEntity.ok(showtimeService.getAllShowtimes());
    }

    @Operation(summary = "Lấy chi tiết một lịch chiếu", description = "Tìm kiếm lịch chiếu theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Integer id) {
        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    @Operation(summary = "Thêm mới lịch chiếu", description = "Tạo mới một lịch chiếu. Hệ thống sẽ tự động tính toán thời gian kết thúc và phụ thu cuối tuần (+15.000đ)")
    @PostMapping
    public ResponseEntity<ShowtimeDTO> createShowtime(@Valid @RequestBody ShowtimeDTO showtimeDTO) {
        return ResponseEntity.ok(showtimeService.createShowtime(showtimeDTO));
    }

    @Operation(summary = "Cập nhật lịch chiếu", description = "Cập nhật thông tin chi tiết của một lịch chiếu đang có")
    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> updateShowtime(@PathVariable Integer id, @Valid @RequestBody ShowtimeDTO showtimeDTO) {
        return ResponseEntity.ok(showtimeService.updateShowtime(id, showtimeDTO));
    }

    @Operation(summary = "Xóa lịch chiếu", description = "Xóa hoàn toàn một lịch chiếu khỏi hệ thống")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShowtime(@PathVariable Integer id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok().build();
    }
}
