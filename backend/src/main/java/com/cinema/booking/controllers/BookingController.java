package com.cinema.booking.controllers;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.dtos.SeatStatusDTO;
import com.cinema.booking.services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking")
@Tag(name = "5. Booking Engine & Redis Locking", description = "Các API đặt vé, khóa ghế (Redis) và tính tiền")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // 1. Vẽ Bản Đồ Ghế (Seat Rendering)
    @Operation(summary = "Lấy sơ đồ ghế và trạng thái", description = "Trả về ma trận ghế của một suất chiếu kèm trạng thái: TRỐNG, ĐÃ BÁN, hoặc ĐANG GIỮ (Redis)")
    @GetMapping("/seats/{showtimeId}")
    public ResponseEntity<List<SeatStatusDTO>> getSeatStatuses(@PathVariable Integer showtimeId) {
        return ResponseEntity.ok(bookingService.getSeatStatuses(showtimeId));
    }

    // 2. Thuật toán Khóa Ghế Redis (SETNX)
    @Operation(summary = "Khóa giữ ghế tạm thời", description = "Sử dụng Redis SETNX để giữ ghế trong 10 phút. Nếu ghế đã bị khóa hoặc đã bán sẽ trả về lỗi.")
    @PostMapping("/lock")
    public ResponseEntity<?> lockSeat(
            @RequestParam Integer showtimeId,
            @RequestParam Integer seatId,
            @RequestParam Integer userId) {
        
        boolean locked = bookingService.lockSeat(showtimeId, seatId, userId);
        if (locked) {
            return ResponseEntity.ok("Ghế đã được giữ trong 10 phút.");
        } else {
            return ResponseEntity.badRequest().body("Ghế đã có người giữ hoặc đã bán.");
        }
    }

    @Operation(summary = "Giải phóng ghế", description = "Xóa khóa giữ ghế trong Redis trước khi hết hạn 10 phút")
    @PostMapping("/unlock")
    public ResponseEntity<?> unlockSeat(
            @RequestParam Integer showtimeId,
            @RequestParam Integer seatId) {
        bookingService.unlockSeat(showtimeId, seatId);
        return ResponseEntity.ok("Đã giải phóng ghế.");
    }

    // 3. Tính tiền (Total Price Calculation)
    @Operation(summary = "Tính toán tổng tiền tạm tính", description = "Tính toán giá vé (+phụ thu) và F&B, áp dụng Voucher nếu có để trả về rổ hàng cho UI")
    @PostMapping("/calculate")
    public ResponseEntity<PriceBreakdownDTO> calculatePrice(@RequestBody BookingCalculationDTO request) {
        return ResponseEntity.ok(bookingService.calculatePrice(request));
    }
}
