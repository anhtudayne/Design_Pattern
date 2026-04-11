package com.cinema.booking.controllers;

import com.cinema.booking.dtos.FnbItemDTO;
import com.cinema.booking.dtos.FnbCategoryDTO;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.FnbCategory;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.repositories.FnbCategoryRepository;
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

    @Autowired
    private FnbCategoryRepository categoryRepository;

    // --- ITEM CRUD ---

    @Operation(summary = "Lấy danh sách tất cả sản phẩm F&B", description = "Trả về toàn bộ danh sách bắp nước kèm thông tin giá và danh mục")
    @GetMapping("/items")
    public List<FnbItemDTO> getAllItems() {
        return itemRepository.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Tạo sản phẩm F&B mới", description = "Thêm một món ăn/nước uống mới theo schema hiện tại.")
    @PostMapping("/items")
    public ResponseEntity<FnbItemDTO> createItem(@RequestBody FnbItemDTO dto) {
        FnbCategory category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }
        FnbItem item = FnbItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .imageUrl(dto.getImageUrl())
                .category(category)
                .build();
        
        return ResponseEntity.ok(toDto(itemRepository.save(item)));
    }

    @Operation(summary = "Cập nhật sản phẩm F&B", description = "Chỉnh sửa thông tin tên, giá, tồn kho và trạng thái sản phẩm.")
    @PutMapping("/items/{id}")
    public ResponseEntity<FnbItemDTO> updateItem(@PathVariable Integer id, @RequestBody FnbItemDTO dto) {
        FnbItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (dto.getCategoryId() != null) {
            FnbCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            item.setCategory(category);
        } else {
            item.setCategory(null);
        }

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setStockQuantity(dto.getStockQuantity());
        item.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : item.getIsActive());
        item.setImageUrl(dto.getImageUrl());
        
        return ResponseEntity.ok(toDto(itemRepository.save(item)));
    }

    @Operation(summary = "Xóa sản phẩm F&B", description = "Xóa hoàn toàn một sản phẩm khởi hệ thống")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        itemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- CATEGORY CRUD ---

    @Operation(summary = "Lấy danh sách Danh mục", description = "Lấy toàn bộ danh mục sản phẩm")
    @GetMapping("/categories")
    public List<FnbCategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryDto).toList();
    }

    @Operation(summary = "Thêm danh mục mới")
    @PostMapping("/categories")
    public ResponseEntity<FnbCategoryDTO> createCategory(@RequestBody FnbCategoryDTO dto) {
        FnbCategory cat = FnbCategory.builder()
                .name(dto.getName())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
        return ResponseEntity.ok(toCategoryDto(categoryRepository.save(cat)));
    }

    @Operation(summary = "Cập nhật danh mục")
    @PutMapping("/categories/{id}")
    public ResponseEntity<FnbCategoryDTO> updateCategory(@PathVariable Integer id, @RequestBody FnbCategoryDTO dto) {
        FnbCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        cat.setName(dto.getName());
        cat.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : cat.getIsActive());
        return ResponseEntity.ok(toCategoryDto(categoryRepository.save(cat)));
    }

    @Operation(summary = "Xóa danh mục")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private FnbCategoryDTO toCategoryDto(FnbCategory cat) {
        FnbCategoryDTO dto = new FnbCategoryDTO();
        dto.setCategoryId(cat.getCategoryId());
        dto.setName(cat.getName());
        dto.setIsActive(cat.getIsActive());
        return dto;
    }

    private FnbItemDTO toDto(FnbItem item) {
        FnbItemDTO dto = new FnbItemDTO();
        dto.setItemId(item.getItemId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setStockQuantity(item.getStockQuantity());
        dto.setIsActive(item.getIsActive());
        dto.setImageUrl(item.getImageUrl());
        dto.setCategoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null);
        return dto;
    }
}
