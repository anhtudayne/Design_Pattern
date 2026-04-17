package com.cinema.booking.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FnbItemDTO {
    private Integer fnbItemId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}
