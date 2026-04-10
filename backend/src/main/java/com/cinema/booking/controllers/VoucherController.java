package com.cinema.booking.controllers;

import com.cinema.booking.dtos.VoucherDTO;
import com.cinema.booking.services.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/vouchers")
@Tag(name = "12. Voucher", description = "API quản lý mã giảm giá nhanh (Lưu trên Redis with TTL)")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    @Operation(summary = "Tạo mới voucher", description = "Tạo mã giảm giá mới và lưu vào Redis với thời gian hết hạn (TTL)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Void> createVoucher(@RequestBody VoucherDTO voucher) {
        voucherService.createVoucher(voucher);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Lấy tất cả voucher", description = "Lấy danh sách các mã giảm giá đang hoạt động trên Redis")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<VoucherDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @Operation(summary = "Cập nhật voucher", description = "Cập nhật thông tin mã giảm giá (giữ nguyên thời gian hết hạn cũ)")
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Void> updateVoucher(@PathVariable String code, @RequestBody VoucherDTO voucher) {
        voucher.setCode(code);
        voucherService.updateVoucher(voucher);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa voucher", description = "Xóa mã giảm giá thủ công khỏi Redis")
    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable String code) {
        voucherService.deleteVoucher(code);
        return ResponseEntity.noContent().build();
    }
}
