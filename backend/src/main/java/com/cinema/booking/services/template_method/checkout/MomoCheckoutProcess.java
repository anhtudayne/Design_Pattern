package com.cinema.booking.services.template_method.checkout;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.MomoPaymentResponse;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.FnbItemInventoryService;
import com.cinema.booking.services.MomoService;
import com.cinema.booking.services.PromotionInventoryService;
import com.cinema.booking.services.factory.BookingFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MomoCheckoutProcess extends AbstractCheckoutTemplate {

    private final MomoService momoService;

    public MomoCheckoutProcess(
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
            MomoService momoService) {
        super(userRepository, ticketRepository, promotionInventoryService, bookingService,
                bookingRepository, fnbItemRepository, fnbItemInventoryService, fnBLineRepository, paymentRepository, bookingFactory);
        this.momoService = momoService;
    }

    @Override
    protected Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request) {
        return Booking.BookingStatus.PENDING;
    }

    @Override
    protected Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) throws Exception {
        // Gửi kèm seatIds qua extraData để khi callback quay lại có thông tin tạo Ticket
        String seatIdsStr = (request.getSeatIds() != null) ? request.getSeatIds().stream().map(String::valueOf).collect(Collectors.joining(",")) : "";
        String extraData = booking.getBookingId() + "|" + request.getShowtimeId() + "|" + seatIdsStr;

        MomoPaymentResponse momoResponse = momoService.createPayment(
                "BOOKING_" + booking.getBookingId(),
                price.getFinalTotal().longValue(),
                "Thanh toán vé xem phim StarCine cho Booking #" + booking.getBookingId(),
                extraData
        );
        return momoResponse.getPayUrl();
    }

    @Override
    protected void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult) {
        try {
            Payment payment = bookingFactory.createPayment(booking, "MOMO", price.getFinalTotal(), Payment.PaymentStatus.PENDING);
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi lưu Payment PENDING: " + e.getMessage());
        }
    }
}
