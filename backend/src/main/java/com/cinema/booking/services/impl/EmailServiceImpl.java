package com.cinema.booking.services.impl;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Ticket;
import com.cinema.booking.patterns.prototype.TicketEmailPrototype;
import com.cinema.booking.patterns.prototype.WelcomeEmailPrototype;
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

    // Prototype singletons — Spring manages their lifecycle
    @Autowired
    private TicketEmailPrototype ticketEmailPrototype;

    @Autowired
    private WelcomeEmailPrototype welcomeEmailPrototype;

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

        // Prototype — copy() → populate → toMessage()
        SimpleMailMessage message = ((TicketEmailPrototype) ticketEmailPrototype.copy())
                .to(email)
                .bookingId(bookingId)
                .customerName(booking.getCustomer().getFullname())
                .movieTitle(movieTitle)
                .showtime(showtimeStr)
                .totalAmount(total)
                .toMessage();

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi Email vé thành công cho Booking ID: " + bookingId);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi Email: " + e.getMessage());
        }
    }

    @Override
    public void sendWelcomeEmail(String email, String fullname) {
        // Prototype — copy() → populate → toMessage()
        SimpleMailMessage message = ((WelcomeEmailPrototype) welcomeEmailPrototype.copy())
                .to(email)
                .fullname(fullname)
                .toMessage();

        try {
            mailSender.send(message);
            System.out.println(">>> Đã gửi email chào mừng thành công cho: " + email);
        } catch (Exception e) {
            System.err.println(">>> Lỗi gửi email chào mừng: " + e.getMessage());
        }
    }
}
