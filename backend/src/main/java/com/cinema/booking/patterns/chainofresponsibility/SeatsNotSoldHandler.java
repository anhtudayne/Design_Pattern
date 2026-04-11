package com.cinema.booking.patterns.chainofresponsibility;

import com.cinema.booking.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatsNotSoldHandler extends AbstractCheckoutValidationHandler {

    private final TicketRepository ticketRepository;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        if (context.getSeatIds() != null) {
            for (Integer seatId : context.getSeatIds()) {
                if (ticketRepository.existsByShowtime_ShowtimeIdAndSeat_SeatId(context.getShowtimeId(), seatId)) {
                    throw new RuntimeException("Ghế ID " + seatId + " đã được bán. Vui lòng chọn ghế khác.");
                }
            }
        }
    }
}
