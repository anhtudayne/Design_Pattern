package com.cinema.booking.services.impl;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Ticket;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.repositories.TicketRepository;
import com.cinema.booking.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public void sendTicketEmail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;

        String email = booking.getCustomer().getUserAccount() != null
                ? booking.getCustomer().getUserAccount().getEmail()
                : null;
        if (email == null) {
            System.err.println(">>> Không gửi được email vé: user không có UserAccount (bookingId=" + bookingId + ")");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Vé xem phim StarCine của bạn #" + bookingId);
        java.util.List<Ticket> tickets = ticketRepository.findByBooking_BookingId(bookingId);
        Ticket firstTicket = tickets.isEmpty() ? null : tickets.get(0);
        String movieTitle = (firstTicket != null && firstTicket.getShowtime() != null && firstTicket.getShowtime().getMovie() != null)
                ? firstTicket.getShowtime().getMovie().getTitle() : "N/A";
        String showtime = (firstTicket != null && firstTicket.getShowtime() != null)
                ? String.valueOf(firstTicket.getShowtime().getStartTime()) : "N/A";
        java.math.BigDecimal total = tickets.stream()
                .map(Ticket::getPrice)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        message.setText("Chào " + booking.getCustomer().getFullname() + ",\n\n" +
                "Cảm ơn bạn đã đặt vé tại StarCine.\n" +
                "Mã số đặt vé: " + bookingId + "\n" +
                "Suất chiếu: " + movieTitle + "\n" +
                "Thời gian: " + showtime + "\n" +
                "Tổng tiền: " + total + " VNĐ\n\n" +
                "Bạn có thể dùng mã booking để nhận vé tại quầy.\n\n" +
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
