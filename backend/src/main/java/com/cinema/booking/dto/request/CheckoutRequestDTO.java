package com.cinema.booking.dto.request;

import lombok.Data;
import com.cinema.booking.dto.BookingCalculationDTO;
import java.util.List;

@Data
public class CheckoutRequestDTO {
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<BookingCalculationDTO.FnbOrderDTO> fnbs;
    private String promoCode;
    /** POST /checkout: MOMO hoặc VNPAY (mặc định MOMO). Tiền mặt: /checkout/cash. */
    private String paymentMethod;
}
