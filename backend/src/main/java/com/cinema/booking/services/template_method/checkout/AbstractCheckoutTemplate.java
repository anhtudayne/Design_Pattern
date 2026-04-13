package com.cinema.booking.services.template_method.checkout;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.factory.BookingFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class AbstractCheckoutTemplate {

    protected final UserRepository userRepository;
    protected final TicketRepository ticketRepository;
    protected final PromotionRepository promotionRepository;
    protected final BookingService bookingService;
    protected final BookingRepository bookingRepository;
    protected final FnbItemRepository fnbItemRepository;
    protected final FnBLineRepository fnBLineRepository;
    protected final PaymentRepository paymentRepository;
    protected final BookingFactory bookingFactory;

    protected AbstractCheckoutTemplate(
            UserRepository userRepository,
            TicketRepository ticketRepository,
            PromotionRepository promotionRepository,
            BookingService bookingService,
            BookingRepository bookingRepository,
            FnbItemRepository fnbItemRepository,
            FnBLineRepository fnBLineRepository,
            PaymentRepository paymentRepository,
            BookingFactory bookingFactory) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.promotionRepository = promotionRepository;
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
        Customer customer = validateUser(request.getUserId());

        // Step 2: Validate Seats
        validateSeats(request.getShowtimeId(), request.getSeatIds());

        // Step 3: Calculate Price
        PriceBreakdownDTO price = calculatePrice(request);

        // Step 4: Find Promotion
        Promotion promotion = findPromotion(request.getPromoCode());

        // Step 5: Determine Booking Status
        Booking.BookingStatus initialBookingStatus = determineInitialBookingStatus(request);

        // Step 6: Create Booking
        Booking booking = createBooking(customer, promotion, initialBookingStatus);

        // Step 7: Save F&B
        saveFnbLines(booking, request.getFnbs());

        // Step 8: Process Payment logic (Method specific)
        Object paymentResult = processPayment(booking, price, request);

        // Step 9: Finalize Booking (Method specific)
        finalizeBooking(booking, price, request, paymentResult);

        return CheckoutResult.builder()
                .booking(booking)
                .price(price)
                .paymentResult(paymentResult)
                .build();
    }

    protected Customer validateUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        if (!(user instanceof Customer)) {
            // Đối tượng mặc định (Khách vãng lai) cho các Staff bán vé tại quầy
            return getOrCreateWalkInGuest();
        }
        return (Customer) user;
    }

    private Customer getOrCreateWalkInGuest() {
        // Tìm xem đã có Khách vãng lai mặc định trong DB chưa (dựa theo SDT 0000000000)
        return userRepository.findByPhone("0000000000")
                .filter(u -> u instanceof Customer)
                .map(u -> (Customer) u)
                .orElseGet(() -> {
                    Customer guest = new Customer();
                    guest.setFullname("Khách Vãng Lai");
                    guest.setPhone("0000000000");
                    guest.setTotalSpending(java.math.BigDecimal.ZERO);
                    guest.setLoyaltyPoints(0);
                    return userRepository.save(guest);
                });
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
        if (promoCode != null && !promoCode.isBlank()) {
            return promotionRepository.findByCode(promoCode).orElse(null);
        }
        return null;
    }

    protected Booking createBooking(Customer customer, Promotion promotion, Booking.BookingStatus status) {
        Booking booking = bookingFactory.createBooking(customer, promotion, status);
        return bookingRepository.save(booking);
    }

    protected void saveFnbLines(Booking booking, List<BookingCalculationDTO.FnbOrderDTO> fnbs) {
        if (fnbs != null && !fnbs.isEmpty()) {
            for (BookingCalculationDTO.FnbOrderDTO fnbDto : fnbs) {
                FnbItem fnbItem = fnbItemRepository.findById(fnbDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + fnbDto.getItemId()));

                FnBLine item = bookingFactory.createFnbLine(booking, fnbItem, fnbDto.getQuantity(), fnbItem.getPrice());
                fnBLineRepository.save(item);
            }
        }
    }

    // --- Abstract methods to be overridden by subclasses ---

    protected abstract Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request);

    protected abstract Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) throws Exception;

    protected abstract void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult);
}
