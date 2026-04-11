package com.cinema.booking.patterns.prototype;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prototype — template cho email vé xem phim (Ticket).
 * Được copy() trước khi điền dữ liệu thực tế.
 */
@Component
public class TicketEmailPrototype implements EmailTemplate {

    private String to;
    private Integer bookingId;
    private String customerName;
    private String movieTitle;
    private String showtime;
    private BigDecimal totalAmount;

    // ── Setters (populated on the copy before sending) ──────────────────────

    public TicketEmailPrototype to(String to) { this.to = to; return this; }
    public TicketEmailPrototype bookingId(Integer bookingId) { this.bookingId = bookingId; return this; }
    public TicketEmailPrototype customerName(String name) { this.customerName = name; return this; }
    public TicketEmailPrototype movieTitle(String title) { this.movieTitle = title; return this; }
    public TicketEmailPrototype showtime(String st) { this.showtime = st; return this; }
    public TicketEmailPrototype totalAmount(BigDecimal amount) { this.totalAmount = amount; return this; }

    @Override
    public EmailTemplate copy() {
        return new TicketEmailPrototype();
    }

    @Override
    public SimpleMailMessage toMessage() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Vé xem phim StarCine của bạn #" + bookingId);
        msg.setText("Chào " + customerName + ",\n\n" +
                "Cảm ơn bạn đã đặt vé tại StarCine.\n" +
                "Mã số đặt vé: " + bookingId + "\n" +
                "Suất chiếu: " + movieTitle + "\n" +
                "Thời gian: " + showtime + "\n" +
                "Tổng tiền: " + totalAmount + " VNĐ\n\n" +
                "Bạn có thể dùng mã booking để nhận vé tại quầy.\n\n" +
                "Trân trọng,\nStarCine Team");
        return msg;
    }
}
