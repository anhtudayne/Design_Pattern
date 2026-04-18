package com.cinema.booking.service.impl;

import com.cinema.booking.entity.Booking;
import com.cinema.booking.entity.Ticket;
import com.cinema.booking.repository.BookingRepository;
import com.cinema.booking.repository.TicketRepository;
import com.cinema.booking.service.EmailService;
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

    @Override
    @Transactional(readOnly = true)
    public void sendTicketEmail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;

        String email = booking.getUser().getUserAccount() != null
                ? booking.getUser().getUserAccount().getEmail()
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
                .map(Ticket::getUnitPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Ve dien tu - Booking #" + bookingId);
        message.setText(
                "Xin chao " + booking.getUser().getFullname() + ",\n\n" +
                "Cam on ban da dat ve tai StarCine.\n" +
                "- Ma booking: " + bookingId + "\n" +
                "- Phim: " + movieTitle + "\n" +
                "- Suat chieu: " + showtimeStr + "\n" +
                "- Tong tien: " + total + "\n\n" +
                "Chuc ban xem phim vui ve!"
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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Chao mung ban den voi StarCine");
        message.setText("Xin chao " + fullname + ",\n\nCam on ban da dang ky tai khoan StarCine.");

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi email chào mừng thành công cho: " + email);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi email chào mừng: " + e.getMessage());
        }
    }
}
