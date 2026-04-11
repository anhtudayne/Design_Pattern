package com.cinema.booking.patterns.chainofresponsibility;

import com.cinema.booking.entities.Showtime;
import com.cinema.booking.entities.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckoutValidationContext {
    private Integer userId;
    private Integer showtimeId;
    private List<Integer> seatIds;
    private String promoCode;
    
    // Cached entities after validation
    private User user;
    private Showtime showtime;
}
