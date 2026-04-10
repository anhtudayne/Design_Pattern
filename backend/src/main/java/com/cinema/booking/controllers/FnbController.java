package com.cinema.booking.controllers;

import com.cinema.booking.dtos.FnbItemDTO;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.repositories.FnbItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fnb")
@Tag(name = "7. Quản lý F&B (Bắp Nước)", description = "Các API CRUD cho Danh mục và Sản phẩm F&B")
public class FnbController {

    @Autowired
    private FnbItemRepository itemRepository;

    // --- ITEM CRUD ---

    @Operation(summary = "Lấy danh sách tất cả sản phẩm F&B", description = "Trả về toàn bộ danh sách bắp nước kèm thông tin giá và danh mục")
    @GetMapping("/items")
    public List<FnbItemDTO> getAllItems() {
        return itemRepository.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Tạo sản phẩm F&B mới", description = "Thêm một món ăn/nước uống mới theo schema hiện tại.")
    @PostMapping("/items")
    public ResponseEntity<FnbItemDTO> createItem(@RequestBody FnbItemDTO dto) {
        FnbItem item = FnbItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
        
        return ResponseEntity.ok(toDto(itemRepository.save(item)));
    }

    @Operation(summary = "Cập nhật sản phẩm F&B", description = "Chỉnh sửa thông tin tên, giá, tồn kho và trạng thái sản phẩm.")
    @PutMapping("/items/{id}")
    public ResponseEntity<FnbItemDTO> updateItem(@PathVariable Integer id, @RequestBody FnbItemDTO dto) {
        FnbItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setStockQuantity(dto.getStockQuantity());
        item.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : item.getIsActive());
        
        return ResponseEntity.ok(toDto(itemRepository.save(item)));
    }

    @Operation(summary = "Xóa sản phẩm F&B", description = "Xóa hoàn toàn một sản phẩm khởi hệ thống")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        itemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Legacy endpoints for old frontend contract. No-op in strict schema mode.
    @GetMapping("/categories")
    public List<Map<String, Object>> getAllCategories() {
        return List.of();
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory() {
        return ResponseEntity.status(501).body(Map.of("message", "F&B categories are not supported in current schema"));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id) {
        return ResponseEntity.status(501).body(Map.of("message", "F&B categories are not supported in current schema"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        return ResponseEntity.status(501).body(Map.of("message", "F&B categories are not supported in current schema"));
    }

    private FnbItemDTO toDto(FnbItem item) {
        FnbItemDTO dto = new FnbItemDTO();
        dto.setItemId(item.getItemId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setStockQuantity(item.getStockQuantity());
        dto.setIsActive(item.getIsActive());
        dto.setImageUrl(null);
        dto.setCategoryId(null);
        return dto;
    }
}
