package com.cinema.booking.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomDTO {
    private Integer roomId;
    
    @NotNull(message = "Mã rạp (Cinema ID) không được trống")
    private Integer cinemaId;
    
    private String cinemaName; // Gói kèm tên rạp theo ID

    @NotBlank(message = "Tên phòng chiếu không được trống")
    private String name;

    private String screenType;
}
