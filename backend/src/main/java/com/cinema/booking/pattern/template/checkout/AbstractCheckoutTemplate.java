package com.cinema.booking.pattern.template.checkout;
import com.cinema.booking.pattern.template.checkout.AbstractCheckoutTemplate;

import com.cinema.booking.dto.BookingCalculationDTO;
import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.response.CheckoutResult;
import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.entity.*;
import com.cinema.booking.repository.*;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.pattern.factory.BookingFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class AbstractCheckoutTemplate {

    protected final UserRepository userRepository;
    protected final TicketRepository ticketRepository;
    protected final BookingService bookingService;
    protected final BookingRepository bookingRepository;
    protected final FnbItemRepository fnbItemRepository;
    protected final FnBLineRepository fnBLineRepository;
    protected final PaymentRepository paymentRepository;
    protected final BookingFactory bookingFactory;

    protected AbstractCheckoutTemplate(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            BookingService bookingService,
            BookingRepository bookingRepository,
            FnbItemRepository fnbItemRepository,
            FnBLineRepository fnBLineRepository,
            PaymentRepository paymentRepository,
            BookingFactory bookingFactory) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.fnbItemRepository = fnbItemRepository;
        this.fnBLineRepository = fnBLineRepository;
        this.paymentRepository = paymentRepository;
        this.bookingFactory = bookingFactory;
    }

    @Transactional
    public final CheckoutResult checkout(CheckoutRequest request) throws Exception {
        // Step 1: Validate User
        User user = validateUser(request.getUserId());

        // Step 2: Validate Seats
        validateSeats(request.getShowtimeId(), request.getSeatIds());

        // Step 3: Calculate Price
        PriceBreakdownDTO price = calculatePrice(request);

        // Step 4: Find Promotion
        Promotion promotion = findPromotion(request.getPromoCode());

        // Step 5: Determine Booking Status
        Booking.BookingStatus initialBookingStatus = determineInitialBookingStatus(request);

        // Step 6: Create Booking
        Booking booking = createBooking(user, promotion, initialBookingStatus);

        // Step 7: Save F&B (Inventory logic removed per strict diagram)
        saveFnbLines(booking, request.getFnbs());

        // Step 8: Process Payment logic (Method specific)
        Object paymentResult = processPayment(booking, price, request);

        // Step 9: Finalize Booking (Method specific)
        finalizeBooking(booking, price, request, paymentResult);

        // Failure/cancelled flows that end in-request may release reserved resources here.
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            rollbackReservedResources(booking);
        }

        return CheckoutResult.builder()
                .booking(booking)
                .price(price)
                .paymentResult(paymentResult)
                .build();
    }

    protected void rollbackReservedResources(Booking booking) {
        // Inventory and specific promotion release logic removed to match strict diagram
    }

    protected User validateUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
    }

    protected void validateSeats(Integer showtimeId, List<Integer> seatIds) {
        for (Integer seatId : seatIds) {
            if (ticketRepository.existsByShowtime_ShowtimeIdAndSeat_SeatId(showtimeId, seatId)) {
                throw new RuntimeException("Ghế ID " + seatId + " đã được bán. Vui lòng chọn ghế khác.");
            }
        }
    }

    protected PriceBreakdownDTO calculatePrice(CheckoutRequest request) {
        BookingCalculationDTO calculationRequest = new BookingCalculationDTO();
        calculationRequest.setShowtimeId(request.getShowtimeId());
        calculationRequest.setSeatIds(request.getSeatIds());
        calculationRequest.setFnbs(request.getFnbs());
        calculationRequest.setPromoCode(request.getPromoCode());
        return bookingService.calculatePrice(calculationRequest);
    }

    protected Promotion findPromotion(String promoCode) {
        // Promotion inventory and code lookup logic removed for strict mode
        return null;
    }

    protected Booking createBooking(User user, Promotion promotion, Booking.BookingStatus status) {
        Booking booking = bookingFactory.createBooking(user, promotion, status);
        return bookingRepository.save(booking);
    }

    protected void saveFnbLines(Booking booking, List<BookingCalculationDTO.FnbOrderDTO> fnbs) {
        if (fnbs != null && !fnbs.isEmpty()) {
            for (BookingCalculationDTO.FnbOrderDTO fnbDto : fnbs) {
                FnbItem fnbItem = fnbItemRepository.findById(fnbDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + fnbDto.getItemId()));

                FnBLine fnbLine = bookingFactory.createFnbLine(booking, fnbItem, fnbDto.getQuantity());
                fnBLineRepository.save(fnbLine);
            }
        }
    }

    // --- Abstract methods to be overridden by subclasses ---

    protected abstract Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request);

    protected abstract Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) throws Exception;

    protected abstract void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult);
}
