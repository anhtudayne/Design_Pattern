package com.cinema.booking.services.payment;

/**
 * Các kênh thanh toán được checkout hỗ trợ. Mở rộng (VNPay, …) bằng cách thêm enum + {@link PaymentStrategy}.
 */
public enum PaymentMethod {
    MOMO,
    DEMO,
    CASH;

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
