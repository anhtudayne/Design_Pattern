package com.cinema.booking.controllers;

import com.cinema.booking.dtos.CheckoutRequestDTO;
import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.services.CheckoutService;
import com.cinema.booking.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
@Tag(name = "6. Thanh Toán & Checkout", description = "Các API thanh toán MoMo, Webhook callback và Đặt vé hoàn tất")
public class PaymentController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private PaymentService paymentService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // 1. Tạo đơn hàng và lấy link MoMo
    @Operation(summary = "Checkout và tạo link thanh toán MoMo", description = "Tạo một bản ghi Booking PENDING và gọi MoMo API để lấy link thanh toán cho khách hàng")
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO request) {
        try {
            String payUrl = checkoutService.createBooking(
                    request.getUserId(),
                    request.getShowtimeId(),
                    request.getSeatIds(),
                    request.getFnbs(),
                    request.getPromoCode()
            );
            return ResponseEntity.ok(java.util.Collections.singletonMap("payUrl", payUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi Checkout: " + e.getMessage());
        }
    }

    // 2. MoMo Redirect Callback (Xử lý và Redirect về Frontend)
    @Operation(summary = "Redirect callback từ MoMo", description = "Hứng redirect từ MoMo sau khi khách thanh toán, xử lý nghiệp vụ và redirect về trang kết quả ở Frontend.")
    @GetMapping("/momo/callback")
    public RedirectView momoCallback(@ModelAttribute MomoCallbackRequest callback) {
        try {
            checkoutService.processMomoCallback(callback);
        } catch (Exception e) {
            System.err.println(">>> [StarCine] Callback Error: " + e.getMessage());
        }
        // Redirect về trang lịch sử giao dịch để người dùng xem kết quả
        String targetUrl = frontendUrl + "/profile/transactions?payment=success&orderId=" + callback.getOrderId();
        if (callback.getResultCode() != 0) {
            targetUrl = frontendUrl + "/profile/transactions?payment=failed&errorCode=" + callback.getResultCode();
        }
        return new RedirectView(targetUrl);
    }

    // 3. MoMo IPN Webhook (Server-to-Server)
    @Operation(summary = "IPN Webhook từ MoMo", description = "MoMo gọi vào API này để thông báo kết quả thanh toán. Nếu thành công, hệ thống sẽ xác nhận vé và gửi email bất đồng bộ.")
    @PostMapping("/momo/webhook")
    public ResponseEntity<?> momoWebhook(@RequestBody MomoCallbackRequest callback) {
        try {
            checkoutService.processMomoCallback(callback);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("IPN Error: " + e.getMessage());
        }
    }

    // 4. API nhận kết quả thanh toán chung (theo yêu cầu user)
    @Operation(summary = "Endpoint nhận kết quả thanh toán và redirect", description = "Endpoint chung để nhận kết quả và chuyển hướng người dùng về frontend")
    @GetMapping("/payment-redirect")
    public RedirectView paymentResult() {
        String targetUrl = frontendUrl + "/profile/transactions";
        return new RedirectView(targetUrl);
    }

    // 5. Lấy lịch sử giao dịch của User
    @Operation(summary = "Lấy lịch sử thanh toán", description = "Lấy danh sách các giao dịch thanh toán của người dùng hiện tại")
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(paymentService.getUserPaymentHistory(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy lịch sử: " + e.getMessage());
        }
    }

    // 6. Lấy chi tiết giao dịch
    @Operation(summary = "Lấy chi tiết thanh toán", description = "Lấy thông tin chi tiết của một mã giao dịch cụ thể")
    @GetMapping("/details/{paymentId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable Integer paymentId) {
        try {
            Payment payment = paymentService.getPaymentDetails(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi lấy chi tiết: " + e.getMessage());
        }
    }
}
