package com.cinema.booking.patterns.mediator;

import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.Showtime;
import com.cinema.booking.entities.Ticket;
import com.cinema.booking.repositories.SeatRepository;
import com.cinema.booking.repositories.ShowtimeRepository;
import com.cinema.booking.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TicketIssuer implements PaymentColleague {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        if (context.getSeatIds() == null || context.getSeatIds().isEmpty() || context.getShowtimeId() == null) {
            System.err.println(">>> [StarCine] BỎ QUA TẠO VÉ: Không có danh sách ghế hoặc showtime.");
            return;
        }

        Showtime showtime = showtimeRepository.findById(context.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Showtime ID: " + context.getShowtimeId()));
        
        int ticketsCreated = 0;
        
        for (Integer seatId : context.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId).orElse(null);
            if (seat != null) {
                BigDecimal ticketPrice = showtime.getBasePrice()
                        .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                                ? seat.getSeatType().getPriceSurcharge()
                                : BigDecimal.ZERO);

                Ticket ticket = Ticket.builder()
                        .booking(context.getBooking())
                        .seat(seat)
                        .showtime(showtime)
                        .price(ticketPrice)
                        .build();
                ticketRepository.save(ticket);
                ticketsCreated++;
            } else {
                System.err.println(">>> [StarCine] WARNING: Không tìm thấy Ghế (Seat) với ID: " + seatId);
            }
        }
        System.out.println(">>> [StarCine] Đã tạo THÀNH CÔNG " + ticketsCreated + " vé cho Booking #" + context.getBooking().getBookingId());
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // Do nothing on failure
    }
}
