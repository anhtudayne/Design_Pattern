package com.cinema.booking.pattern.chain;
import com.cinema.booking.pattern.chain.ShowtimeFutureHandler;
import com.cinema.booking.pattern.chain.SeatsAvailableHandler;
import com.cinema.booking.pattern.chain.AbstractPricingValidationHandler;
import com.cinema.booking.pattern.chain.PricingValidationContext;

import com.cinema.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validate tất cả seat được chọn chưa được bán cho suất chiếu này.
 * Phụ thuộc ShowtimeFutureHandler chạy trước (cần showtimeId trong request).
 */
@Component
@RequiredArgsConstructor
public class SeatsAvailableHandler extends AbstractPricingValidationHandler {

    private final TicketRepository ticketRepository;

    @Override
    protected void doValidate(PricingValidationContext context) {
        List<Integer> seatIds = context.getRequest().getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất 1 ghế.");
        }

        Integer showtimeId = context.getRequest().getShowtimeId();
        for (Integer seatId : seatIds) {
            if (ticketRepository.existsByShowtime_ShowtimeIdAndSeat_SeatId(showtimeId, seatId)) {
                throw new RuntimeException("Ghế ID " + seatId + " đã được bán. Vui lòng chọn ghế khác.");
            }
        }
    }
}
