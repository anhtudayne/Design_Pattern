package com.cinema.booking.controllers;

import com.cinema.booking.dtos.FnbItemDTO;
import com.cinema.booking.dtos.FnbCategoryDTO;
import com.cinema.booking.entities.Cinema;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.FnbCategory;
import com.cinema.booking.repositories.CinemaRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.repositories.FnbCategoryRepository;
import com.cinema.booking.services.FnbItemInventoryService;
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

    @Autowired
    private FnbItemInventoryService fnbItemInventoryService;

    @Autowired
    private CinemaRepository cinemaRepository;

    // --- ITEM CRUD ---

    @Operation(summary = "Lấy danh sách sản phẩm F&B", description = "Theo chi nhánh nếu truyền cinemaId; không truyền thì trả về toàn hệ thống (admin).")
    @GetMapping("/items")
    public List<FnbItemDTO> getAllItems(@RequestParam(required = false) Integer cinemaId) {
        List<FnbItem> items = cinemaId != null
                ? itemRepository.findByCinema_CinemaId(cinemaId)
                : itemRepository.findAll();
        Map<Integer, Integer> quantityMap = fnbItemInventoryService.getQuantityMap(
                items.stream().map(FnbItem::getItemId).toList()
        );
        return items.stream().map(item -> toDto(item, quantityMap)).toList();
    }

    @Operation(summary = "Tạo sản phẩm F&B mới", description = "Thêm một món ăn/nước uống mới theo schema hiện tại.")
    @PostMapping("/items")
    public ResponseEntity<FnbItemDTO> createItem(@RequestBody FnbItemDTO dto) {
        if (dto.getCinemaId() == null) {
            throw new RuntimeException("cinemaId là bắt buộc");
        }
        Cinema cinema = cinemaRepository.findById(dto.getCinemaId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp (cinema)"));
        FnbCategory category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }
        FnbItem item = FnbItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .imageUrl(dto.getImageUrl())
                .category(category)
                .cinema(cinema)
                .build();

        FnbItem saved = itemRepository.save(item);
        fnbItemInventoryService.upsertQuantity(saved, dto.getStockQuantity());
        return ResponseEntity.ok(toDto(saved, Map.of(saved.getItemId(), fnbItemInventoryService.getQuantity(saved.getItemId()))));
    }

    @Operation(summary = "Cập nhật sản phẩm F&B", description = "Chỉnh sửa thông tin tên, giá, tồn kho và trạng thái sản phẩm.")
    @PutMapping("/items/{id}")
    public ResponseEntity<FnbItemDTO> updateItem(@PathVariable Integer id, @RequestBody FnbItemDTO dto) {
        FnbItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (dto.getCinemaId() != null) {
            Cinema cinema = cinemaRepository.findById(dto.getCinemaId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy rạp (cinema)"));
            item.setCinema(cinema);
        }

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
        item.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : item.getIsActive());
        item.setImageUrl(dto.getImageUrl());

        FnbItem saved = itemRepository.save(item);
        fnbItemInventoryService.upsertQuantity(saved, dto.getStockQuantity());
        return ResponseEntity.ok(toDto(saved, Map.of(saved.getItemId(), fnbItemInventoryService.getQuantity(saved.getItemId()))));
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

    private FnbItemDTO toDto(FnbItem item, Map<Integer, Integer> quantityMap) {
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
    }
}
