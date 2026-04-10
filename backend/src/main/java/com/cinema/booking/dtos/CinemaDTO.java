package com.cinema.booking.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CinemaDTO {
    private Integer cinemaId;
    
    @NotNull(message = "Mã Tỉnh/Thành phố (Location ID) không được bỏ trống")
    private Integer locationId;
    
    // Thuộc tính phụ (Read-only) dùng để bung chuỗi text hiển thị trên UI Frontend
    private String locationName; 

    @NotBlank(message = "Tên Cụm rạp không được bỏ trống")
    private String name;

    @NotBlank(message = "Địa chỉ rạp không được bỏ trống")
    private String address;
    private String hotline;

}
