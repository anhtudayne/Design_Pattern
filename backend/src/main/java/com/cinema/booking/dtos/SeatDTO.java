package com.cinema.booking.dtos;

import com.cinema.booking.entities.Seat.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeatDTO {
    private Integer seatId;
    
    @NotNull(message = "Mã Phòng (Room ID) không được bỏ trống")
    private Integer roomId;
    
    @NotBlank(message = "Hàng ghế (Row) không được bỏ ráp")
    private String seatRow; // A, B, C...
    
    @NotNull(message = "Số ghế trống")
    private Integer seatNumber; // 1, 2, 3...
    
    private SeatType seatType;
    private BigDecimal priceSurcharge; // Phụ thu ghế Vip/Đôi
    private Boolean isActive;
}
