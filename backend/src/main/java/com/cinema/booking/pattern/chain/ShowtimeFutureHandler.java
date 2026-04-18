package com.cinema.booking.pattern.chain;
import com.cinema.booking.pattern.chain.ShowtimeFutureHandler;
import com.cinema.booking.pattern.chain.AbstractPricingValidationHandler;
import com.cinema.booking.pattern.chain.PricingValidationContext;

import com.cinema.booking.entity.Showtime;
import com.cinema.booking.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validate showtime tồn tại và chưa chiếu (startTime trong tương lai).
 * Populate {@link PricingValidationContext#setShowtime(Showtime)} để các handler sau và
 * BookingServiceImpl tái dùng mà không cần load lại DB.
 */
@Component
@RequiredArgsConstructor
public class ShowtimeFutureHandler extends AbstractPricingValidationHandler {

    private final ShowtimeRepository showtimeRepository;

    @Override   
    protected void doValidate(PricingValidationContext context) {
        Showtime showtime = showtimeRepository.findById(context.getRequest().getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại."));

        if (showtime.getStartTime() != null && showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Suất chiếu đã kết thúc, không thể đặt vé.");
        }

        context.setShowtime(showtime);
    }
}
