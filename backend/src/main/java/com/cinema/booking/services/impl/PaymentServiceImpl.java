package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.PaymentHistoryDTO;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.entities.Ticket;
import com.cinema.booking.repositories.TicketRepository;
import com.cinema.booking.repositories.PaymentRepository;
import com.cinema.booking.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<PaymentHistoryDTO> getUserPaymentHistory(Integer userId) {
        return paymentRepository.findUserPaymentHistory(userId).stream()
                .map(this::toHistoryDto)
                .toList();
    }

    @Override
    public Payment getPaymentDetails(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại với ID: " + paymentId));
    }

    private PaymentHistoryDTO toHistoryDto(Payment payment) {
        Integer bookingId = payment.getBooking() != null ? payment.getBooking().getBookingId() : null;
        String bookingCode = payment.getBooking() != null ? payment.getBooking().getBookingCode() : null;

        String movieTitle = null;
        String moviePoster = null;
        if (bookingId != null) {
            List<Ticket> tickets = ticketRepository.findByBooking_BookingId(bookingId);
            if (!tickets.isEmpty() && tickets.get(0).getShowtime() != null && tickets.get(0).getShowtime().getMovie() != null) {
                movieTitle = tickets.get(0).getShowtime().getMovie().getTitle();
                moviePoster = tickets.get(0).getShowtime().getMovie().getPosterUrl();
            }
        }

        return PaymentHistoryDTO.builder()
                .paymentId(payment.getPaymentId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus() != null ? payment.getStatus().name() : "PENDING")
                .paidAt(payment.getPaidAt())
                .booking(PaymentHistoryDTO.BookingSummary.builder()
                        .bookingId(bookingId)
                        .bookingCode(bookingCode)
                        .showtime(PaymentHistoryDTO.ShowtimeSummary.builder()
                                .movie(PaymentHistoryDTO.MovieSummary.builder()
                                        .title(movieTitle)
                                        .posterUrl(moviePoster)
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
