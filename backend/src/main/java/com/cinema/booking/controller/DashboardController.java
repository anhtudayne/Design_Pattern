package com.cinema.booking.controller;

import com.cinema.booking.entity.Payment;
import com.cinema.booking.pattern.composite.DashboardStatsComposite;
import com.cinema.booking.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "14. Dashboard Thống kê", description = "Các API cung cấp dữ liệu tổng quan cho trang quản trị")
public class DashboardController {

    /** Thống kê tổng quan qua composite (một lần gọi {@code collect}). */
    @Autowired
    private DashboardStatsComposite dashboardStatsComposite;

    /** Dùng cho API doanh thu theo tuần (chưa gộp vào composite). */
    @Autowired
    private PaymentRepository paymentRepository;

    @Operation(summary = "Lấy số liệu tổng quan hệ thống")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        dashboardStatsComposite.collect(stats);
        return ResponseEntity.ok(stats);
    }
    @Operation(summary = "Lấy dữ liệu doanh thu 7 ngày gần nhất")
    @GetMapping("/revenue-weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyRevenue() {
        List<Payment> payments = paymentRepository.findByStatus(Payment.PaymentStatus.SUCCESS).stream()
                .filter(p -> p.getPaidAt() != null)
                .toList();

        // Khởi tạo map cho 7 ngày trong tuần
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        java.util.Map<Integer, BigDecimal> dailyData = new java.util.HashMap<>();
        for (int i = 1; i <= 7; i++) dailyData.put(i, BigDecimal.ZERO);

        for (Payment p : payments) {
            java.time.LocalDateTime date = p.getPaidAt();
            // dayOfWeek: 1 (Mon) to 7 (Sun)
            int day = date.getDayOfWeek().getValue();
            dailyData.put(day, dailyData.get(day).add(p.getAmount()));
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("day", days[i - 1]);
            item.put("amount", dailyData.get(i));
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }
}
