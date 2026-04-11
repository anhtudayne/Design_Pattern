package com.cinema.booking.patterns.mediator;

import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.entities.Booking;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MomoCallbackContext {
    private MomoCallbackRequest callback;
    private Booking booking;
    private List<Integer> seatIds;
    private Integer showtimeId;
    private boolean success;
}
