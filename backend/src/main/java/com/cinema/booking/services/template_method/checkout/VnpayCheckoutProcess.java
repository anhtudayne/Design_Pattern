package com.cinema.booking.services.template_method.checkout;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.dtos.VnpayPaymentResponse;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.VnPayService;
import com.cinema.booking.services.factory.BookingFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Checkout VNPay — PENDING + redirect URL (cổng thật khi bật {@code vnpay.enabled}).
 */
@Component
public class VnpayCheckoutProcess extends AbstractCheckoutTemplate {

    private final VnPayService vnPayService;

    public VnpayCheckoutProcess(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            BookingService bookingService,
            BookingRepository bookingRepository,
            FnbItemRepository fnbItemRepository,
            FnBLineRepository fnBLineRepository,
            PaymentRepository paymentRepository,
            BookingFactory bookingFactory,
            VnPayService vnPayService) {
        super(userRepository, ticketRepository, bookingService,
                bookingRepository, fnbItemRepository, fnBLineRepository, paymentRepository, bookingFactory);
        this.vnPayService = vnPayService;
    }

    @Override
    protected Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request) {
        return Booking.BookingStatus.PENDING;
    }

    @Override
    protected Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) throws Exception {
        String seatIdsStr = (request.getSeatIds() != null)
                ? request.getSeatIds().stream().map(String::valueOf).collect(Collectors.joining(","))
                : "";
        String extraData = booking.getBookingId() + "|" + request.getShowtimeId() + "|" + seatIdsStr;

        VnpayPaymentResponse response = vnPayService.createPayment(
                "BOOKING_" + booking.getBookingId(),
                price.getFinalTotal().longValue(),
                "Thanh toán vé xem phim StarCine (VNPay) cho Booking #" + booking.getBookingId(),
                extraData
        );
        return response.getPayUrl();
    }

    @Override
    protected void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult) {
        try {
            Payment payment = bookingFactory.createPayment(
                    booking, "VNPAY", price.getFinalTotal(), Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi lưu Payment VNPAY PENDING: " + e.getMessage());
        }
    }
}
