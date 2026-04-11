package com.cinema.booking.patterns.chainofresponsibility;

import com.cinema.booking.entities.Showtime;
import com.cinema.booking.repositories.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeExistsHandler extends AbstractCheckoutValidationHandler {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        Showtime showtime = showtimeRepository.findById(context.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));
        
        context.setShowtime(showtime);
    }
}
