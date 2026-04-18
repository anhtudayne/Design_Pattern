package com.cinema.booking.controller;

import com.cinema.booking.dto.PromotionDTO;
import com.cinema.booking.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/promotions")
@Tag(name = "12. Khuyến mãi (Promotion)", description = "CRUD mã giảm giá lưu trong database")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @Operation(summary = "Danh sách promotion")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<PromotionDTO>> list() {
        return ResponseEntity.ok(promotionService.findAll());
    }

    @Operation(summary = "Tạo promotion")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<PromotionDTO> create(@RequestBody PromotionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.create(dto));
    }

    @Operation(summary = "Cập nhật promotion")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<PromotionDTO> update(@PathVariable Integer id, @RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(promotionService.update(id, dto));
    }

    @Operation(summary = "Xóa promotion")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
