package com.cinema.booking.services.payment;

/**
 * Kênh thanh toán map 1-1 với {@link PaymentStrategy} (Factory đăng ký đủ enum).
 * <ul>
 *   <li>MOMO, VNPAY — cổng online (redirect), dùng {@code POST /api/payment/checkout}</li>
 *   <li>CASH — tiền mặt / quầy, {@code POST /api/payment/checkout/cash} hoặc staff cash-checkout</li>
 * </ul>
 */
public enum PaymentMethod {
    MOMO,
    CASH,
    VNPAY;

    public static PaymentMethod fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return MOMO;
        }
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ: " + raw);
        }
    }
}
