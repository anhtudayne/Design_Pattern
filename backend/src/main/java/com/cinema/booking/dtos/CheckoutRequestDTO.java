package com.cinema.booking.dtos;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequestDTO {
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<BookingCalculationDTO.FnbOrderDTO> fnbs;
    private String promoCode;
    /** MOMO (mặc định). Giá trị khác DEMO không dùng ở /checkout — dùng /checkout/demo. */
    private String paymentMethod;
}
