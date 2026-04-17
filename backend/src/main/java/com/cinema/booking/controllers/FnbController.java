package com.cinema.booking.controllers;

import com.cinema.booking.dtos.FnbItemDTO;
import com.cinema.booking.entities.FnbItem;
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
@Tag(name = "7. Quản lý F&B (Bắp Nước)", description = "Các API CRUD cho sản phẩm F&B")
public class FnbController {

    @Autowired
    private FnbItemRepository itemRepository;

    // --- ITEM CRUD ---

    @Operation(summary = "Lấy danh sách sản phẩm F&B")
    @GetMapping("/items")
    public List<FnbItemDTO> getAllItems() {
        return itemRepository.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Tạo sản phẩm F&B mới")
    @PostMapping("/items")
    public ResponseEntity<FnbItemDTO> createItem(@RequestBody FnbItemDTO dto) {
        FnbItem item = FnbItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .build();

        FnbItem saved = itemRepository.save(item);
        return ResponseEntity.ok(toDto(saved));
    }

    @Operation(summary = "Cập nhật sản phẩm F&B")
    @PutMapping("/items/{id}")
    public ResponseEntity<FnbItemDTO> updateItem(@PathVariable Integer id, @RequestBody FnbItemDTO dto) {
        FnbItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setImageUrl(dto.getImageUrl());

        FnbItem saved = itemRepository.save(item);
        return ResponseEntity.ok(toDto(saved));
    }

    @Operation(summary = "Xóa sản phẩm F&B", description = "Xóa hoàn toàn một sản phẩm khởi hệ thống")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer id) {
        itemRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private FnbItemDTO toDto(FnbItem item) {
        FnbItemDTO dto = new FnbItemDTO();
        dto.setFnbItemId(item.getFnbItemId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setImageUrl(item.getImageUrl());
        return dto;
    }
}
