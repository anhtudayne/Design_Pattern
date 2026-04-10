package com.cinema.booking.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeatDTO {
    private Integer seatId;
    
    @NotNull(message = "Mã Phòng (Room ID) không được bỏ trống")
    private Integer roomId;
    
    private String seatCode;
    private String seatRow;
    private Integer seatNumber;

    @NotNull(message = "seatTypeId là bắt buộc")
    private Integer seatTypeId;

    // output fields
    private String seatTypeName;
    private BigDecimal seatTypeSurcharge;
    private Boolean isActive;
}
