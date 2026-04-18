package com.cinema.booking.dto.request;

import lombok.Data;
import com.cinema.booking.dto.BookingCalculationDTO;

import java.util.List;

/** Body cho xác nhận thanh toán MoMo mô phỏng trên UI (QR + nút thành công/thất bại). */
@Data
public class MomoUiFinishRequest {
    private boolean success;
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<BookingCalculationDTO.FnbOrderDTO> fnbs;
    private String promoCode;
}
