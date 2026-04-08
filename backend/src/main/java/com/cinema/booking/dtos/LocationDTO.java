package com.cinema.booking.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationDTO {
    private Integer locationId;
    
    @NotBlank(message = "Tên Tỉnh/Thành phố không được bỏ trống")
    private String name;
}
