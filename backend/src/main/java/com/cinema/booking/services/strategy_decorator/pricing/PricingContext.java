package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.Showtime;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PricingContext {
    private Showtime showtime;
    private List<Seat> seats;
    private List<BookingCalculationDTO.FnbOrderDTO> fnbs;
    private Promotion promotion;
}
