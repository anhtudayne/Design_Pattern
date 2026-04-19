package com.cinema.booking.controller;

import com.cinema.booking.dto.request.CheckoutRequestDTO;
import com.cinema.booking.dto.request.MomoCallbackRequest;
import com.cinema.booking.dto.request.MomoUiFinishRequest;
import com.cinema.booking.dto.request.VnpayUiFinishRequest;
import com.cinema.booking.entity.Payment;
import com.cinema.booking.security.UserDetailsImpl;
import com.cinema.booking.service.CheckoutService;
import com.cinema.booking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
@Slf4j
@Tag(name = "6. Thanh Toán & Checkout", description = "Tiền mặt, MoMo/VNPay cổng + QR mô phỏng, callback MoMo")
public class PaymentController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private PaymentService paymentService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Operation(summary = "Checkout cổng online", description = "POST /checkout — MOMO hoặc VNPAY → payUrl (Booking PENDING).")
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO request) throws Exception {
        String payUrl = checkoutService.createBooking(
                request.getUserId(),
                request.getShowtimeId(),
                request.getSeatIds(),
                request.getFnbs(),
                request.getPromoCode(),
                request.getPaymentMethod()
        );
        if (payUrl == null || payUrl.isBlank()) {
            return ResponseEntity.badRequest().body("Không tạo được link thanh toán. Kiểm tra cấu hình MoMo / VNPay.");
        }
        return ResponseEntity.ok(java.util.Collections.singletonMap("payUrl", payUrl));
    }

    @Operation(
            summary = "Checkout tiền mặt (khách web)",
            description = "CashPaymentStrategy + StaffCashCheckoutProcess. JWT phải trùng userId."
    )
    @PostMapping("/checkout/cash")
    public ResponseEntity<?> customerCashCheckout(
            @RequestBody CheckoutRequestDTO request,
            Authentication authentication
    ) throws Exception {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập.");
        }
        if (request.getUserId() == null || !request.getUserId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không được đặt vé thay tài khoản khác.");
        }
        return ResponseEntity.ok(checkoutService.processCustomerCashCheckout(
                request.getUserId(),
                request.getShowtimeId(),
                request.getSeatIds(),
                request.getFnbs(),
                request.getPromoCode()
        ));
    }

    @Operation(summary = "Hoàn tất MoMo (QR mô phỏng)", description = "LocalMomoCheckoutProcess — không thay thế cổng redirect.")
    @PostMapping("/checkout/momo-ui/finish")
    public ResponseEntity<?> momoUiFinish(
            @RequestBody MomoUiFinishRequest request,
            Authentication authentication
    ) throws Exception {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập.");
        }
        if (request.getUserId() == null || !request.getUserId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không được đặt vé thay tài khoản khác.");
        }
        return ResponseEntity.ok(checkoutService.processMomoUiFinish(
                request.isSuccess(),
                request.getUserId(),
                request.getShowtimeId(),
                request.getSeatIds(),
                request.getFnbs(),
                request.getPromoCode()
        ));
    }

    @Operation(summary = "Hoàn tất VNPay (QR mô phỏng)", description = "LocalVnpayCheckoutProcess.")
    @PostMapping("/checkout/vnpay-ui/finish")
    public ResponseEntity<?> vnpayUiFinish(
            @RequestBody VnpayUiFinishRequest request,
            Authentication authentication
    ) throws Exception {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập.");
        }
        if (request.getUserId() == null || !request.getUserId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không được đặt vé thay tài khoản khác.");
        }
        return ResponseEntity.ok(checkoutService.processVnpayUiFinish(
                request.isSuccess(),
                request.getUserId(),
                request.getShowtimeId(),
                request.getSeatIds(),
                request.getFnbs(),
                request.getPromoCode()
        ));
    }

    @Operation(summary = "Redirect callback từ MoMo")
    @GetMapping("/momo/callback")
    public RedirectView momoCallback(@ModelAttribute MomoCallbackRequest callback) {
        try {
            checkoutService.processMomoCallback(callback);
        } catch (Exception e) {
            log.error("MoMo callback error: {}", e.getMessage(), e);
        }
        String targetUrl = frontendUrl + "/profile/transactions?payment=success&orderId=" + callback.getOrderId();
        if (callback.getResultCode() != null && callback.getResultCode() != 0) {
            targetUrl = frontendUrl + "/profile/transactions?payment=failed&errorCode=" + callback.getResultCode();
        }
        return new RedirectView(targetUrl);
    }

    @Operation(summary = "IPN Webhook MoMo")
    @PostMapping("/momo/webhook")
    public ResponseEntity<?> momoWebhook(@RequestBody MomoCallbackRequest callback) throws Exception {
        checkoutService.processMomoCallback(callback);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Chuyển hướng về frontend")
    @GetMapping("/payment-redirect")
    public RedirectView paymentResult() {
        return new RedirectView(frontendUrl + "/profile/transactions");
    }

    @Operation(summary = "Lấy lịch sử thanh toán")
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(paymentService.getUserPaymentHistory(userId));
    }

    @Operation(summary = "Chi tiết thanh toán")
    @GetMapping("/details/{paymentId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable Integer paymentId) {
        Payment payment = paymentService.getPaymentDetails(paymentId);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Staff bán vé tiền mặt")
    @PostMapping("/staff/cash-checkout")
    public ResponseEntity<?> staffCashCheckout(@RequestBody CheckoutRequestDTO request) throws Exception {
        return ResponseEntity.ok(checkoutService.processStaffCashCheckout(
                request.getUserId(),
                request.getShowtimeId(),
                request.getSeatIds(),
                request.getFnbs(),
                request.getPromoCode()
        ));
    }
}
