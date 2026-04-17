package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.services.CheckoutService;
import com.cinema.booking.services.MomoService;
import com.cinema.booking.services.payment.CashPaymentStrategy;
import com.cinema.booking.services.payment.DemoPaymentStrategy;
import com.cinema.booking.services.payment.PaymentMethod;
import com.cinema.booking.services.payment.PaymentStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Value("${momo.dev-all-success}")
    private boolean devAllSuccess;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MomoService momoService;

    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;

    // Mediator pattern removed - post-payment tasks need to be re-implemented
    // @Autowired
    // private PostPaymentMediator postPaymentMediator;

    @Override
    @Transactional
    public String createBooking(Integer userId, Integer showtimeId, List<Integer> seatIds,
                                List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode,
                                String paymentMethod) throws Exception {
        PaymentMethod method = PaymentMethod.fromString(paymentMethod);
        if (method != PaymentMethod.MOMO) {
            throw new IllegalArgumentException(
                    "Endpoint checkout chuẩn chỉ hỗ trợ MOMO. Luồng thử không MoMo dùng API /checkout/demo.");
        }

        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .build();

        CheckoutResult result = paymentStrategyFactory.getStrategy(method).checkout(request);
        return (String) result.getPaymentResult();
    }

    @Override
    @Transactional
    public void processMomoCallback(MomoCallbackRequest callback) throws Exception {
        // TODO: Re-implement post-payment tasks without Mediator pattern
        // The following tasks need to be handled:
        // 1. Update Booking status to CONFIRMED
        // 2. Update Customer totalSpending
        // 3. Create Tickets for each seat
        // 4. Update Payment status to COMPLETED
        // 5. Send ticket email to customer
        
        throw new UnsupportedOperationException(
            "Post-payment processing needs to be re-implemented after Mediator pattern removal. " +
            "Please implement the 5 post-payment tasks directly in this method."
        );
        
        /* Original code with Mediator pattern:
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
                    } catch (NumberFormatException ignored) {
                    }
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
        */
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> processDemoCheckout(
            Integer userId,
            Integer showtimeId,
            List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode,
            boolean success
    ) throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .demoSuccess(success)
                .build();

        DemoPaymentStrategy demoStrategy = paymentStrategyFactory.getStrategy(PaymentMethod.DEMO, DemoPaymentStrategy.class);
        CheckoutResult result = demoStrategy.checkout(request);

        return demoStrategy.buildDemoResult(
                result.getBooking(),
                (Payment) result.getPaymentResult(),
                result.getPrice()
        );
    }

    /**
     * Factory Method: {@link PaymentMethod#CASH} → {@link CashPaymentStrategy} (template {@code StaffCashCheckoutProcess}).
     */
    private java.util.Map<String, Object> executeCashCheckout(CheckoutRequest request) throws Exception {
        CashPaymentStrategy cashStrategy = paymentStrategyFactory.getStrategy(PaymentMethod.CASH, CashPaymentStrategy.class);
        CheckoutResult result = cashStrategy.checkout(request);
        return cashStrategy.buildResult(
                result.getBooking(),
                (Payment) result.getPaymentResult(),
                result.getPrice()
        );
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> processStaffCashCheckout(
            Integer customerId, Integer showtimeId, List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(customerId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .demoSuccess(true)
                .build();
        return executeCashCheckout(request);
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> processCustomerCashCheckout(
            Integer userId, Integer showtimeId, List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .demoSuccess(true)
                .build();
        return executeCashCheckout(request);
    }
}
