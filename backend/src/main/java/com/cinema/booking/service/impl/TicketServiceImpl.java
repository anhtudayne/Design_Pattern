package com.cinema.booking.service.impl;

import com.cinema.booking.dto.TicketDTO;
import com.cinema.booking.entity.Ticket;
import com.cinema.booking.repository.TicketRepository;
import com.cinema.booking.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<TicketDTO> getTicketsByBooking(Integer bookingId) {
        return ticketRepository.findByBooking_BookingId(bookingId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByUser(Integer userId) {
        return ticketRepository.findByBooking_User_UserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TicketDTO getTicketDetails(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ticket ID: " + ticketId));
        return convertToDTO(ticket);
    }

    @Override
    public void deleteTicket(Integer ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException("Không tìm thấy Ticket ID: " + ticketId);
        }
        ticketRepository.deleteById(ticketId);
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        return TicketDTO.builder()
                .ticketId(ticket.getTicketId())
                .movieId(ticket.getMovie() != null ? ticket.getMovie().getMovieId() : null)
                .showtimeId(ticket.getShowtime() != null ? ticket.getShowtime().getShowtimeId() : null)
                .seatId(ticket.getSeat() != null ? ticket.getSeat().getSeatId() : null)
                .unitPrice(ticket.getUnitPrice())
                .holdExpiresAt(ticket.getHoldExpiresAt())
                .bookingId(ticket.getBooking() != null ? ticket.getBooking().getBookingId() : null)
                .build();
    }
}
