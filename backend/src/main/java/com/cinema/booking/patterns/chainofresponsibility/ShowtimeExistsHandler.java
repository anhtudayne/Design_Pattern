package com.cinema.booking.patterns.chainofresponsibility;

import com.cinema.booking.entities.Showtime;
import com.cinema.booking.repositories.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShowtimeExistsHandler extends AbstractCheckoutValidationHandler {

    private final ShowtimeRepository showtimeRepository;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        Showtime showtime = showtimeRepository.findById(context.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));
        context.setShowtime(showtime);
    }
}
