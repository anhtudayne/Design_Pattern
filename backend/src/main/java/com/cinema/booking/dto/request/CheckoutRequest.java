package com.cinema.booking.dto.request;

import com.cinema.booking.dto.BookingCalculationDTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutRequest {
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<BookingCalculationDTO.FnbOrderDTO> fnbs;
    private String promoCode;

    /** {@link com.cinema.booking.pattern.template.checkout.LocalMomoCheckoutProcess} */
    private boolean momoUiPaid;

    /** {@link com.cinema.booking.pattern.template.checkout.LocalVnpayCheckoutProcess} */
    private boolean vnpayUiPaid;
}
