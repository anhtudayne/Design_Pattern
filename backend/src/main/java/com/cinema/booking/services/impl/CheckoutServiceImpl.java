package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.dtos.MomoPaymentResponse;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.patterns.chainofresponsibility.CheckoutValidationContext;
import com.cinema.booking.patterns.chainofresponsibility.CheckoutValidationHandler;
import com.cinema.booking.patterns.mediator.MomoCallbackContext;
import com.cinema.booking.patterns.mediator.PostPaymentMediator;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.CheckoutService;
import com.cinema.booking.services.MomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Value("${momo.dev-all-success}")
    private boolean devAllSuccess;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MomoService momoService;

    @Autowired
    private FnBLineRepository fnBLineRepository;

    @Autowired
    private FnbItemRepository fnbItemRepository;

    @Autowired
    private CheckoutValidationHandler checkoutValidationChain;

    @Autowired
    private PostPaymentMediator postPaymentMediator;

    @Override
    @Transactional
    public String createBooking(Integer userId, Integer showtimeId, List<Integer> seatIds,
                                List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception {
        CheckoutValidationContext context = CheckoutValidationContext.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .promoCode(promoCode)
                .build();
        checkoutValidationChain.handle(context);

        Customer customer = (Customer) context.getUser();
        Promotion promotion = promoCode != null ? promotionRepository.findByCode(promoCode).orElse(null) : null;

        BookingCalculationDTO calculationRequest = new BookingCalculationDTO();
        calculationRequest.setShowtimeId(showtimeId);
        calculationRequest.setSeatIds(seatIds);
        calculationRequest.setFnbs(fnbs);
        calculationRequest.setPromoCode(promoCode);
        PriceBreakdownDTO price = bookingService.calculatePrice(calculationRequest);

        Booking booking = Booking.builder()
                .customer(customer)
                .promotion(promotion)
                .status(Booking.BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        if (fnbs != null && !fnbs.isEmpty()) {
            for (BookingCalculationDTO.FnbOrderDTO fnbDto : fnbs) {
                FnbItem fnbItem = fnbItemRepository.findById(fnbDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + fnbDto.getItemId()));
                FnBLine item = FnBLine.builder()
                        .booking(booking)
                        .item(fnbItem)
                        .quantity(fnbDto.getQuantity())
                        .unitPrice(fnbItem.getPrice())
                        .build();
                fnBLineRepository.save(item);
            }
        }

        String seatIdsStr = (seatIds != null)
                ? seatIds.stream().map(String::valueOf).collect(Collectors.joining(",")) : "";
        String extraData = booking.getBookingId() + "|" + showtimeId + "|" + seatIdsStr;

        MomoPaymentResponse momoResponse = momoService.createPayment(
                "BOOKING_" + booking.getBookingId(),
                price.getFinalTotal().longValue(),
                "Thanh toán vé xem phim StarCine cho Booking #" + booking.getBookingId(),
                extraData
        );

        try {
            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod("MOMO")
                    .amount(price.getFinalTotal())
                    .status(Payment.PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi lưu Payment PENDING: " + e.getMessage());
        }

        return momoResponse.getPayUrl();
    }

    @Override
    @Transactional
    public void processMomoCallback(MomoCallbackRequest callback) throws Exception {
        if (!momoService.verifySignature(callback)) {
            throw new RuntimeException("Chữ ký MoMo không hợp lệ");
        }

        if (callback.getExtraData() == null || callback.getExtraData().isEmpty()) {
            throw new RuntimeException("Thiếu extraData (bookingId) từ MoMo callback");
        }

        String extraData = callback.getExtraData();
        String[] parts = extraData.split("\\|");
        Integer bookingId = Integer.parseInt(parts[0]);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking ID: " + bookingId));

        boolean success = devAllSuccess || callback.getResultCode() == 0;

        List<Integer> seatIds = null;
        Integer showtimeId = null;

        if (success) {
            System.out.println(">>> [StarCine] rawExtraData nhận được: " + extraData);

            if (extraData.contains("%7C")) {
                try {
                    extraData = java.net.URLDecoder.decode(extraData, "UTF-8");
                    System.out.println(">>> [StarCine] Decoded extraData thành công: " + extraData);
                    parts = extraData.split("\\|");
                } catch (java.io.UnsupportedEncodingException e) {
                    System.err.println(">>> [StarCine] ERROR khi decode extraData: " + e.getMessage());
                }
            }

            if (parts.length > 2 && !parts[2].isEmpty()) {
                showtimeId = Integer.parseInt(parts[1]);
                String[] seatIdStrs = parts[2].split(",");
                seatIds = new java.util.ArrayList<>();
                for (String s : seatIdStrs) {
                    try {
                        seatIds.add(Integer.parseInt(s.trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        MomoCallbackContext context = MomoCallbackContext.builder()
                .callback(callback)
                .booking(booking)
                .seatIds(seatIds)
                .showtimeId(showtimeId)
                .success(success)
                .build();

        if (success) {
            System.out.println(">>> [StarCine] Xử lý thanh toán THÀNH CÔNG cho Booking #" + bookingId);
            postPaymentMediator.settleSuccess(context);
        } else {
            System.out.println(">>> [StarCine] Xử lý thanh toán THẤT BẠI cho Booking #" + bookingId
                    + " (resultCode=" + callback.getResultCode() + ")");
            postPaymentMediator.settleFailure(context);
        }
    }

    /**
     * Demo checkout — simulates a completed payment without going through MoMo.
     * Uses the same PostPaymentMediator as the real callback path to avoid logic duplication.
     */
    @Override
    @Transactional
    public Map<String, Object> processDemoCheckout(
            Integer userId,
            Integer showtimeId,
            List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode,
            boolean success
    ) throws Exception {
        // 1. Validate via Chain of Responsibility
        CheckoutValidationContext validationCtx = CheckoutValidationContext.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .promoCode(promoCode)
                .build();
        checkoutValidationChain.handle(validationCtx);

        Customer customer = (Customer) validationCtx.getUser();
        Promotion promotion = promoCode != null ? promotionRepository.findByCode(promoCode).orElse(null) : null;

        // 2. Calculate price
        BookingCalculationDTO calculationRequest = new BookingCalculationDTO();
        calculationRequest.setShowtimeId(showtimeId);
        calculationRequest.setSeatIds(seatIds);
        calculationRequest.setFnbs(fnbs);
        calculationRequest.setPromoCode(promoCode);
        PriceBreakdownDTO price = bookingService.calculatePrice(calculationRequest);

        // 3. Create booking as PENDING — Mediator will confirm or cancel it
        Booking booking = Booking.builder()
                .customer(customer)
                .promotion(promotion)
                .status(Booking.BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        // 4. Save F&B lines
        if (fnbs != null && !fnbs.isEmpty()) {
            for (BookingCalculationDTO.FnbOrderDTO fnbDto : fnbs) {
                FnbItem fnbItem = fnbItemRepository.findById(fnbDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + fnbDto.getItemId()));
                FnBLine item = FnBLine.builder()
                        .booking(booking)
                        .item(fnbItem)
                        .quantity(fnbDto.getQuantity())
                        .unitPrice(fnbItem.getPrice())
                        .build();
                fnBLineRepository.save(item);
            }
        }

        // 5. Create PENDING payment so PaymentStatusUpdater can locate and update it
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("MOMO")
                .amount(price.getFinalTotal())
                .status(Payment.PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        // 6. Build a synthetic callback context and delegate to PostPaymentMediator
        MomoCallbackRequest fakeCallback = new MomoCallbackRequest();
        fakeCallback.setAmount(price.getFinalTotal().longValue());

        MomoCallbackContext mediatorCtx = MomoCallbackContext.builder()
                .callback(fakeCallback)
                .booking(booking)
                .seatIds(seatIds)
                .showtimeId(showtimeId)
                .success(success)
                .build();

        if (success) {
            postPaymentMediator.settleSuccess(mediatorCtx);
        } else {
            postPaymentMediator.settleFailure(mediatorCtx);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("bookingId", booking.getBookingId());
        result.put("bookingCode", booking.getBookingCode());
        result.put("paymentStatus", success
                ? Payment.PaymentStatus.SUCCESS.name()
                : Payment.PaymentStatus.FAILED.name());
        result.put("totalAmount", price.getFinalTotal());
        return result;
    }
}
