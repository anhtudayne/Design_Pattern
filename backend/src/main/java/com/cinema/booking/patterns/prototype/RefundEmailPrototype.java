package com.cinema.booking.patterns.prototype;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Prototype — template cho email hoàn tiền (Refund).
 * Dự phòng cho tính năng refund trong tương lai.
 */
@Component
public class RefundEmailPrototype implements EmailTemplate {

    private String to;
    private String customerName;
    private Integer bookingId;
    private BigDecimal refundAmount;

    // ── Setters (populated on the copy before sending) ──────────────────────

    public RefundEmailPrototype to(String to) { this.to = to; return this; }
    public RefundEmailPrototype customerName(String name) { this.customerName = name; return this; }
    public RefundEmailPrototype bookingId(Integer bookingId) { this.bookingId = bookingId; return this; }
    public RefundEmailPrototype refundAmount(BigDecimal amount) { this.refundAmount = amount; return this; }

    @Override
    public EmailTemplate copy() {
        return new RefundEmailPrototype();
    }

    @Override
    public SimpleMailMessage toMessage() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Hoàn tiền đặt vé StarCine #" + bookingId);
        msg.setText("Chào " + customerName + ",\n\n" +
                "Chúng tôi đã xử lý hoàn tiền cho đơn đặt vé #" + bookingId + ".\n" +
                "Số tiền hoàn: " + refundAmount + " VNĐ\n\n" +
                "Vui lòng chờ 3-5 ngày làm việc để nhận lại tiền.\n\n" +
                "Trân trọng,\nStarCine Team");
        return msg;
    }
}
