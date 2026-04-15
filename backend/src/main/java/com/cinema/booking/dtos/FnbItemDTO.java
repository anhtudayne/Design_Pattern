package com.cinema.booking.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FnbItemDTO {
    private Integer itemId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
    // Backward-compatible fields for old frontend contracts
    private String imageUrl;
    private Integer categoryId;
    /** Tên danh mục — chỉ để hiển thị */
    private String categoryName;
    /** Chi nhánh / rạp */
    private Integer cinemaId;
    private String cinemaName;
}
