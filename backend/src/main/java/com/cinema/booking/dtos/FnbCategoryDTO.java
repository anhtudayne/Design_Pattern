package com.cinema.booking.dtos;

import lombok.Data;

@Data
public class FnbCategoryDTO {
    private Integer categoryId;
    private String name;
    private Boolean isActive;
}
