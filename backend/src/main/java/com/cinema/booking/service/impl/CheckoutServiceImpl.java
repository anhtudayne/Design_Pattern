package com.cinema.booking.service.impl;
import com.cinema.booking.pattern.strategy.payment.PaymentStrategyFactory;
import com.cinema.booking.pattern.strategy.payment.PaymentMethod;
import com.cinema.booking.pattern.strategy.payment.CashPaymentStrategy;
import com.cinema.booking.pattern.template.checkout.StaffCashCheckoutProcess;

import com.cinema.booking.dto.BookingCalculationDTO;
import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.response.CheckoutResult;
import com.cinema.booking.dto.request.MomoCallbackRequest;
import com.cinema.booking.entity.Payment;
import com.cinema.booking.service.CheckoutService;
import com.cinema.booking.service.MomoService;
import com.cinema.booking.pattern.template.checkout.LocalMomoCheckoutProcess;
import com.cinema.booking.pattern.template.checkout.LocalVnpayCheckoutProcess;
import com.cinema.booking.pattern.strategy.payment.CashPaymentStrategy;
import com.cinema.booking.pattern.strategy.payment.PaymentMethod;
import com.cinema.booking.pattern.strategy.payment.PaymentStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Autowired
    private MomoService momoService;

    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;

    @Autowired
    private LocalMomoCheckoutProcess localMomoCheckoutProcess;

    @Autowired
    private LocalVnpayCheckoutProcess localVnpayCheckoutProcess;

    // Mediator pattern removed - post-payment tasks need to be re-implemented
    // @Autowired
    // private PostPaymentMediator postPaymentMediator;

    @Override
    @Transactional
    public String createBooking(Integer userId, Integer showtimeId, List<Integer> seatIds,
                                List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode,
                                String paymentMethod) throws Exception {
        PaymentMethod method = PaymentMethod.fromString(paymentMethod);
        if (method != PaymentMethod.MOMO && method != PaymentMethod.VNPAY) {
            throw new IllegalArgumentException(
                    "POST /checkout chỉ hỗ trợ MOMO hoặc VNPAY. Tiền mặt: POST /checkout/cash.");
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
        if (!momoService.verifySignature(callback)) {
            throw new RuntimeException("Chữ ký MoMo không hợp lệ");
        }
        // TODO: sau callback thành công — CONFIRM booking, UPDATE payment, tạo vé, email (logic tách dần khỏi Mediator cũ).
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
                .build();
        return executeCashCheckout(request);
    }

    @Override
    @Transactional
    public Map<String, Object> processMomoUiFinish(
            boolean success,
            Integer userId,
            Integer showtimeId,
            List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode) throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .momoUiPaid(success)
                .build();

        CheckoutResult result = localMomoCheckoutProcess.checkout(request);
        return localMomoCheckoutProcess.buildUiResult(
                result.getBooking(),
                (Payment) result.getPaymentResult(),
                result.getPrice());
    }

    @Override
    @Transactional
    public Map<String, Object> processVnpayUiFinish(
            boolean success,
            Integer userId,
            Integer showtimeId,
            List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode) throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .vnpayUiPaid(success)
                .build();

        CheckoutResult result = localVnpayCheckoutProcess.checkout(request);
        return localVnpayCheckoutProcess.buildUiResult(
                result.getBooking(),
                (Payment) result.getPaymentResult(),
                result.getPrice());
    }
}
