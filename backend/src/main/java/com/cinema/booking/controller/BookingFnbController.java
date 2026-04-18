package com.cinema.booking.controller;

import com.cinema.booking.entity.FnBLine;
import com.cinema.booking.service.BookingFnbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cinema.booking.dto.request.BookingFnbCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/booking-fnbs")
@Tag(name = "10. Booking FNB", description = "API quản lý Booking FNB cho đơn hàng")
public class BookingFnbController {

    @Autowired
    private BookingFnbService bookingFnbService;

    @Operation(summary = "Tạo mới Booking FNB Items", description = "Tạo danh sách đồ ăn và thức uống mới cho một mã đặt vé")
    @PostMapping
    public ResponseEntity<List<FnBLine>> createBookingFnbItems(@RequestBody BookingFnbCreateDTO createDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingFnbService.createBookingFnbItems(createDTO));
    }

    @Operation(summary = "Lấy tất cả danh sách Booking FNB Items", description = "Truy xuất toàn bộ danh sách các item đồ ăn và thức uống đã đặt")
    @GetMapping
    public ResponseEntity<List<FnBLine>> getAllBookingFnbItems() {
        return ResponseEntity.ok(bookingFnbService.getAllBookingFnbItems());
    }

    @Operation(summary = "Lấy danh sách Booking FNB Items theo Booking ID", description = "Truy xuất các item đồ ăn và thức uống liên kết với một mã đặt vé cụ thể")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<FnBLine>> getBookingFnbByBookingId(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingFnbService.getBookingFnbItemsByBookingId(bookingId));
    }

    @Operation(summary = "Xóa tất cả Booking FNB Items của một Booking", description = "Xóa tất cả các item đồ ăn và thức uống theo mã đặt vé")
    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<Void> deleteBookingFnbsByBookingId(@PathVariable Integer bookingId) {
        bookingFnbService.deleteBookingFnbItemsByBookingId(bookingId);
        return ResponseEntity.noContent().build();
    }
}
