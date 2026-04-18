package com.cinema.booking.dtos;

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

    /** {@link com.cinema.booking.services.template_method.checkout.LocalMomoCheckoutProcess} */
    private boolean momoUiPaid;

    /** {@link com.cinema.booking.services.template_method.checkout.LocalVnpayCheckoutProcess} */
    private boolean vnpayUiPaid;
}
