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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // Prototype pattern removed - using inline templates now

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

        List<Ticket> tickets = ticketRepository.findByBooking_BookingId(bookingId);
        Ticket firstTicket = tickets.isEmpty() ? null : tickets.get(0);
        String movieTitle = (firstTicket != null && firstTicket.getShowtime() != null && firstTicket.getShowtime().getMovie() != null)
                ? firstTicket.getShowtime().getMovie().getTitle() : "N/A";
        String showtimeStr = (firstTicket != null && firstTicket.getShowtime() != null)
                ? String.valueOf(firstTicket.getShowtime().getStartTime()) : "N/A";
        BigDecimal total = tickets.stream()
                .map(Ticket::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build email message inline (prototype pattern removed)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Vé xem phim StarCine của bạn #" + bookingId);
        message.setText(
            "Chào " + booking.getCustomer().getFullname() + ",\n\n" +
            "Cảm ơn bạn đã đặt vé tại StarCine.\n" +
            "Mã số đặt vé: " + bookingId + "\n" +
            "Phim: " + movieTitle + "\n" +
            "Suất chiếu: " + showtimeStr + "\n" +
            "Tổng tiền: " + total + " VNĐ\n\n" +
            "Dùng mã booking để nhận vé tại quầy.\n\n" +
            "Trân trọng,\nStarCine Team"
        );

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi Email vé thành công cho Booking ID: " + bookingId);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi Email: " + e.getMessage());
        }
    }

    @Override
    public void sendWelcomeEmail(String email, String fullname) {
        // Build email message inline (prototype pattern removed)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Chào mừng bạn đến với StarCine!");
        message.setText(
            "Chào " + fullname + ",\n\n" +
            "Cảm ơn bạn đã đăng ký tài khoản tại StarCine.\n" +
            "Chúc bạn có những trải nghiệm xem phim tuyệt vời!\n\n" +
            "Trân trọng,\nStarCine Team"
        );

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi email chào mừng thành công cho: " + email);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi email chào mừng: " + e.getMessage());
        }
    }
}
