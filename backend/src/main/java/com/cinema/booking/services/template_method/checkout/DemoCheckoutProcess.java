package com.cinema.booking.services.template_method.checkout;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.EmailService;
import com.cinema.booking.services.factory.BookingFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class DemoCheckoutProcess extends AbstractCheckoutTemplate {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final EmailService emailService;
    private final MovieRepository movieRepository;

    public DemoCheckoutProcess(
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
            EmailService emailService,
            MovieRepository movieRepository) {
        super(userRepository, ticketRepository, bookingService,
                bookingRepository, fnbItemRepository, fnBLineRepository, paymentRepository, bookingFactory);
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.emailService = emailService;
        this.movieRepository = movieRepository;
    }

    @Override
    protected Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request) {
        return request.isDemoSuccess() ? Booking.BookingStatus.CONFIRMED : Booking.BookingStatus.CANCELLED;
    }

    @Override
    protected Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) throws Exception {
        // Tạo Payment record ngay (SUCCESS hoặc FAILED tuỳ thuộc vào demoSuccess flag)
        Payment.PaymentStatus paymentStatus = request.isDemoSuccess() ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED;
        Payment payment = bookingFactory.createPayment(booking, "MOMO", price.getFinalTotal(), paymentStatus);
        paymentRepository.save(payment);
        return payment;
    }

    @Override
    protected void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult) {
        if (request.isDemoSuccess()) {
            // Tạo Ticket cho từng ghế
            Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                    .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));

            Movie movie = movieRepository.findById(showtime.getMovie().getMovieId())
                    .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));

            for (Integer seatId : request.getSeatIds()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Ghế không tồn tại: " + seatId));
                
                // Note: basePrice is kept in Showtime in Phase 1 despite not being in original diagram, 
                // to support existing price calculation logic during this migration phase.
                BigDecimal ticketPrice = showtime.getBasePrice()
                        .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                                ? seat.getSeatType().getPriceSurcharge()
                                : BigDecimal.ZERO);

                Ticket ticket = bookingFactory.createTicket(booking, seat, showtime, movie, ticketPrice);
                ticketRepository.save(ticket);
            }

            // Gửi email vé
            try {
                emailService.sendTicketEmail(booking.getBookingId());
            } catch (Exception e) {
                System.err.println(">>> [StarCine] ERROR gửi email demo checkout: " + e.getMessage());
            }
        }
    }

    public Map<String, Object> buildDemoResult(Booking booking, Payment payment, PriceBreakdownDTO price) {
        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", booking.getBookingId());
        result.put("bookingCode", booking.getBookingCode());
        result.put("paymentStatus", payment.getStatus().name());
        result.put("totalAmount", price.getFinalTotal());
        return result;
    }
}
