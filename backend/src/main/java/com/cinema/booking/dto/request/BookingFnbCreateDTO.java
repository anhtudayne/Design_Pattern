package com.cinema.booking.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BookingFnbCreateDTO {
    private Integer bookingId;
    private List<FnbItemDTO> items;

    @Data
    public static class FnbItemDTO {
        private Integer itemId;
        private Integer quantity;
    }
}
