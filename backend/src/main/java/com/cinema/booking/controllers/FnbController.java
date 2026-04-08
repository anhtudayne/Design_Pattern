package com.cinema.booking.controllers;

import com.cinema.booking.dtos.FnbItemDTO;
import com.cinema.booking.entities.FnbCategory;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.repositories.FnbCategoryRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fnb")
@Tag(name = "7. Quản lý F&B (Bắp Nước)", description = "Các API CRUD cho Danh mục và Sản phẩm F&B")
public class FnbController {

    @Autowired
    private FnbCategoryRepository categoryRepository;

    @Autowired
    private FnbItemRepository itemRepository;

    // --- CATEGORY CRUD ---

    @Operation(summary = "Lấy danh sách danh mục F&B", description = "Trả về toàn bộ danh mục sản phẩm bắp nước hiện có")
    @GetMapping("/categories")
    public List<FnbCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Operation(summary = "Tạo danh mục F&B mới", description = "Thêm một danh mục mới vào hệ thống (VD: Bắp, Nước, Combo)")
    @PostMapping("/categories")
    public FnbCategory createCategory(@RequestBody FnbCategory category) {
        return categoryRepository.save(category);
    }

    @Operation(summary = "Cập nhật danh mục F&B", description = "Thay đổi thông tin tên hoặc trạng thái của một danh mục theo ID")
    @PutMapping("/categories/{id}")
    public ResponseEntity<FnbCategory> updateCategory(@PathVariable Integer id, @RequestBody FnbCategory details) {
        FnbCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(details.getName());
        category.setIsActive(details.getIsActive());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @Operation(summary = "Xóa danh mục F&B", description = "Xóa hoàn toàn một danh mục khỏi hệ thống")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- ITEM CRUD ---

    @Operation(summary = "Lấy danh sách tất cả sản phẩm F&B", description = "Trả về toàn bộ danh sách bắp nước kèm thông tin giá và danh mục")
    @GetMapping("/items")
    public List<FnbItem> getAllItems() {
        return itemRepository.findAll();
    }

    @Operation(summary = "Tạo sản phẩm F&B mới", description = "Thêm một món ăn/nước uống mới. Cần cung cấp categoryId để liên kết.")
    @PostMapping("/items")
    public ResponseEntity<FnbItem> createItem(@RequestBody FnbItemDTO dto) {
        FnbCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        FnbItem item = FnbItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .category(category)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
        
        return ResponseEntity.ok(itemRepository.save(item));
    }

    @Operation(summary = "Cập nhật sản phẩm F&B", description = "Chỉnh sửa thông tên, giá, hình ảnh hoặc danh mục của sản phẩm")
    @PutMapping("/items/{id}")
    public ResponseEntity<FnbItem> updateItem(@PathVariable Integer id, @RequestBody FnbItemDTO dto) {
        FnbItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        if (dto.getCategoryId() != null) {
            FnbCategory category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            item.setCategory(category);
        }
        
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setImageUrl(dto.getImageUrl());
        item.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : item.getIsActive());
        
        return ResponseEntity.ok(itemRepository.save(item));
    }

    @Operation(summary = "Xóa sản phẩm F&B", description = "Xóa hoàn toàn một sản phẩm khởi hệ thống")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        itemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
