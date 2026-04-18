package com.cinema.booking.dto.request;

import lombok.Data;
import com.cinema.booking.dto.BookingCalculationDTO;

import java.util.List;

/** Body xác nhận thanh toán VNPay mô phỏng (QR + nút thành công/thất bại). */
@Data
public class VnpayUiFinishRequest {
    private boolean success;
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<BookingCalculationDTO.FnbOrderDTO> fnbs;
    private String promoCode;
}
