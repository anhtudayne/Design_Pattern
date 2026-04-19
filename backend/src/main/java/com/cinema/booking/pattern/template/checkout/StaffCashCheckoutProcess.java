package com.cinema.booking.pattern.template.checkout;

import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.entity.*;
import com.cinema.booking.repository.*;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.pattern.factory.BookingFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Template Method Pattern: thanh toán tiền mặt (POS nhân viên hoặc khách chọn tiền mặt trên web).
 *  - Không gọi cổng thanh toán online, không có redirect URL
 *  - Booking CONFIRMED ngay; Payment method = CASH, status = SUCCESS
 */
@Component
public class StaffCashCheckoutProcess extends AbstractCheckoutTemplate {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;

    public StaffCashCheckoutProcess(
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
            MovieRepository movieRepository) {
        super(userRepository, ticketRepository, bookingService,
                bookingRepository, fnbItemRepository, fnBLineRepository, paymentRepository, bookingFactory);
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * Staff nhận tiền mặt ngay → Booking đã xác nhận tức thì.
     */
    @Override
    protected Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request) {
        return Booking.BookingStatus.CONFIRMED;
    }

    /**
     * Tạo Payment record với method = CASH, status = SUCCESS.
     * Không cần gọi bất kỳ Gateway bên ngoài.
     */
    @Override
    protected Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) {
        Payment payment = bookingFactory.createPayment(booking, "CASH", price.getFinalTotal(), Payment.PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        return payment;
    }

    /**
     * Finalise: Tạo Ticket cho từng ghế.
     */
    @Override
    protected void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult) {
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
    }

    public Map<String, Object> buildResult(Booking booking, Payment payment, PriceBreakdownDTO price) {
        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", booking.getBookingId());
        result.put("bookingCode", booking.getBookingCode());
        result.put("paymentMethod", "CASH");
        result.put("paymentStatus", payment.getStatus().name());
        result.put("totalAmount", price.getFinalTotal());
        result.put("confirmedAt", LocalDateTime.now().toString());
        return result;
    }
}
