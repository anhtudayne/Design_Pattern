package com.cinema.booking.patterns.prototype;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

/**
 * Prototype — template cho email chào mừng thành viên mới (Welcome).
 */
@Component
public class WelcomeEmailPrototype implements EmailTemplate {

    private String to;
    private String fullname;

    // ── Setters (populated on the copy before sending) ──────────────────────

    public WelcomeEmailPrototype to(String to) { this.to = to; return this; }
    public WelcomeEmailPrototype fullname(String name) { this.fullname = name; return this; }

    @Override
    public EmailTemplate copy() {
        WelcomeEmailPrototype clone = new WelcomeEmailPrototype();
        clone.to = this.to;
        clone.fullname = this.fullname;
        return clone;
    }

    @Override
    public SimpleMailMessage toMessage() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Chào mừng bạn đến với StarCine!");
        msg.setText("Chào " + fullname + ",\n\n" +
                "Chào mừng bạn đã gia nhập gia đình StarCine! " +
                "Chúng tôi rất vui mừng khi thấy bạn đăng ký tài khoản.\n\n" +
                "Hãy bắt đầu đặt vé và tận hưởng những bộ phim hay nhất ngay hôm nay!\n\n" +
                "Trân trọng,\nStarCine Team");
        return msg;
    }
}
