package com.cinema.booking.pattern.template.checkout;

import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.entity.*;
import com.cinema.booking.repository.*;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.service.EmailService;
import com.cinema.booking.pattern.factory.BookingFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MoMo qua UI mô phỏng: không gọi cổng test-payment — khách quét QR giả và bấm thành công/thất bại.
 */
@Component
public class LocalMomoCheckoutProcess extends AbstractCheckoutTemplate {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final EmailService emailService;

    public LocalMomoCheckoutProcess(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            BookingService bookingService,
            BookingRepository bookingRepository,
            FnbItemRepository fnbItemRepository,
            FnBLineRepository fnBLineRepository,
            PaymentRepository paymentRepository,
            BookingFactory bookingFactory,
            ShowtimeRepository showtimeRepository,
            SeatRepository seatRepository,
            MovieRepository movieRepository,
            EmailService emailService) {
        super(userRepository, ticketRepository, bookingService,
                bookingRepository, fnbItemRepository, fnBLineRepository, paymentRepository, bookingFactory);
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
        this.emailService = emailService;
    }

    @Override
    protected Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request) {
        return request.isMomoUiPaid() ? Booking.BookingStatus.CONFIRMED : Booking.BookingStatus.CANCELLED;
    }

    @Override
    protected Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) {
        Payment.PaymentStatus status = request.isMomoUiPaid()
                ? Payment.PaymentStatus.SUCCESS
                : Payment.PaymentStatus.FAILED;
        Payment payment = bookingFactory.createPayment(booking, "MOMO", price.getFinalTotal(), status);
        paymentRepository.save(payment);
        return payment;
    }

    @Override
    protected void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult) {
        if (!request.isMomoUiPaid()) {
            return;
        }
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại: " + request.getShowtimeId()));

        Movie movie = movieRepository.findById(showtime.getMovie().getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));

        for (Integer seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Ghế không tồn tại: " + seatId));

            BigDecimal ticketPrice = showtime.getBasePrice()
                    .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                            ? seat.getSeatType().getPriceSurcharge()
                            : BigDecimal.ZERO);

            Ticket ticket = bookingFactory.createTicket(booking, seat, showtime, movie, ticketPrice);
            ticketRepository.save(ticket);
        }

        try {
            emailService.sendTicketEmail(booking.getBookingId());
        } catch (Exception e) {
            System.err.println(">>> [StarCine] Email vé (MoMo UI): " + e.getMessage());
        }
    }

    public Map<String, Object> buildUiResult(Booking booking, Payment payment, PriceBreakdownDTO price) {
        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", booking.getBookingId());
        result.put("bookingCode", booking.getBookingCode());
        result.put("paymentMethod", "MOMO");
        result.put("paymentStatus", payment.getStatus().name());
        result.put("totalAmount", price.getFinalTotal());
        result.put("confirmedAt", LocalDateTime.now().toString());
        return result;
    }
}
