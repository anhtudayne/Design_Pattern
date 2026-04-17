package com.cinema.booking.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FnbItemDTO {
    private Integer FnbItem_ID;
    private String name;
    private String description;
    private BigDecimal price;
}
