package com.cinema.booking.services.impl;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public void sendTicketEmail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getUser().getEmail());
        message.setSubject("Vé xem phim StarCine của bạn #" + bookingId);
        message.setText("Chào " + booking.getUser().getFullname() + ",\n\n" +
                "Cảm ơn bạn đã đặt vé tại StarCine.\n" +
                "Mã số đặt vé: " + bookingId + "\n" +
                "Suất chiếu: " + booking.getShowtime().getMovie().getTitle() + "\n" +
                "Thời gian: " + booking.getShowtime().getStartTime() + "\n" +
                "Tổng tiền: " + booking.getTotalPrice() + " VNĐ\n\n" +
                "Bạn có thể dùng mã QR đính kèm hoặc mã ID để nhận vé tại quầy.\n\n" +
                "Trân trọng,\nStarCine Team");

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi Email vé thành công cho Booking ID: " + bookingId);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi Email: " + e.getMessage());
        }
    }
    @Override
    public void sendWelcomeEmail(String email, String fullname) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Chào mừng bạn đến với StarCine!");
        message.setText("Chào " + fullname + ",\n\n" +
                "Chào mừng bạn đã gia nhập gia đình StarCine! " +
                "Chúng tôi rất vui mừng khi thấy bạn đăng ký tài khoản.\n\n" +
                "Hãy bắt đầu đặt vé và tận hưởng những bộ phim hay nhất ngay hôm nay!\n\n" +
                "Trân trọng,\nStarCine Team");

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi email chào mừng thành công cho: " + email);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi email chào mừng: " + e.getMessage());
        }
    }
}
