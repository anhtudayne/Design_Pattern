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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DemoCheckoutProcess extends AbstractCheckoutTemplate {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final CustomerRepository customerRepository;
    private final NotificationPublisher notificationPublisher;

    public DemoCheckoutProcess(
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

            // Cập nhật tổng chi tiêu cho Customer
            safeIncreaseCustomerSpending(booking.getCustomer().getUserId(), price.getFinalTotal());

            // Publish notification event for booking confirmed (async)
            notificationPublisher.publishAsync(NotificationPayload.builder()
                    .eventType(NotificationEvent.BOOKING_CONFIRMED)
                    .primaryId(booking.getBookingId().longValue())
                    .build());
        }
    }

    /**
     * Build kết quả trả về cho PaymentController (demo checkout).
     * Method này được CheckoutServiceImpl gọi sau khi template checkout() hoàn thành.
     */
    public Map<String, Object> buildDemoResult(Booking booking, Payment payment, PriceBreakdownDTO price) {
        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", booking.getBookingId());
        result.put("bookingCode", booking.getBookingCode());
        result.put("paymentStatus", payment.getStatus().name());
        result.put("totalAmount", price.getFinalTotal());
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
                if (!msg.contains("deadlock")) {
                    throw ex;
                }
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
