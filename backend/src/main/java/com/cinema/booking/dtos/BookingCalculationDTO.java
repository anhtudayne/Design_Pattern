package com.cinema.booking.dtos;

import lombok.Data;
import java.util.List;

@Data
public class BookingCalculationDTO {
    private Integer showtimeId;
    private List<Integer> seatIds;
    private List<FnbOrderDTO> fnbs;
    private String promoCode;

    @Data
    public static class FnbOrderDTO {
        private Integer itemId;
        private Integer quantity;
    }
}
