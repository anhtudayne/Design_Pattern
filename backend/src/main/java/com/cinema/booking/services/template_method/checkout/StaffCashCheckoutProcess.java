package com.cinema.booking.services.template_method.checkout;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.FnbItemInventoryService;
import com.cinema.booking.services.PromotionInventoryService;
import com.cinema.booking.services.factory.BookingFactory;
import com.cinema.booking.services.notification.NotificationEvent;
import com.cinema.booking.services.notification.NotificationPayload;
import com.cinema.booking.services.notification.NotificationPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Template Method Pattern: thanh toán tiền mặt (POS nhân viên hoặc khách chọn tiền mặt trên web).
 *  - Không gọi cổng thanh toán online, không có redirect URL
 *  - Booking CONFIRMED ngay; Payment method = CASH, status = SUCCESS
 */
@Component
public class StaffCashCheckoutProcess extends AbstractCheckoutTemplate {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final CustomerRepository customerRepository;
    private final NotificationPublisher notificationPublisher;

    public StaffCashCheckoutProcess(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            PromotionInventoryService promotionInventoryService,
            BookingService bookingService,
            BookingRepository bookingRepository,
            FnbItemRepository fnbItemRepository,
            FnbItemInventoryService fnbItemInventoryService,
            FnBLineRepository fnBLineRepository,
            PaymentRepository paymentRepository,
            BookingFactory bookingFactory,
            ShowtimeRepository showtimeRepository,
            SeatRepository seatRepository,
            CustomerRepository customerRepository,
            NotificationPublisher notificationPublisher) {
        super(userRepository, ticketRepository, promotionInventoryService, bookingService,
                bookingRepository, fnbItemRepository, fnbItemInventoryService, fnBLineRepository, paymentRepository, bookingFactory);
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.customerRepository = customerRepository;
        this.notificationPublisher = notificationPublisher;
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
     * Finalise: Tạo Ticket cho từng ghế, cập nhật totalSpending của khách.
     */
    @Override
    protected void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại: " + request.getShowtimeId()));

        for (Integer seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Ghế không tồn tại: " + seatId));
            BigDecimal ticketPrice = showtime.getBasePrice()
                    .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                            ? seat.getSeatType().getPriceSurcharge()
                            : BigDecimal.ZERO);

            Ticket ticket = bookingFactory.createTicket(booking, seat, showtime, ticketPrice);
            ticketRepository.save(ticket);
        }

        // Update customer spending
        safeIncreaseCustomerSpending(booking.getCustomer().getUserId(), price.getFinalTotal());
        
        // Publish notification event for booking confirmed (async)
        notificationPublisher.publishAsync(NotificationPayload.builder()
                .eventType(NotificationEvent.BOOKING_CONFIRMED)
                .primaryId(booking.getBookingId().longValue())
                .build());
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

    private void safeIncreaseCustomerSpending(Integer userId, BigDecimal amount) {
        RuntimeException last = null;
        for (int i = 0; i < 3; i++) {
            try {
                customerRepository.increaseTotalSpending(userId, amount);
                return;
            } catch (RuntimeException ex) {
                last = ex;
                String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                if (!msg.contains("deadlock")) throw ex;
                try {
                    TimeUnit.MILLISECONDS.sleep(120L * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw last != null ? last : new RuntimeException("Không thể cập nhật tổng chi tiêu khách hàng");
    }
}
