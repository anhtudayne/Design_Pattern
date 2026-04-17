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
    private String imageUrl;
}
