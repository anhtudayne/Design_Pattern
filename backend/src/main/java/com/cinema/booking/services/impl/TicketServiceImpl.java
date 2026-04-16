package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.TicketDTO;
import com.cinema.booking.entities.Ticket;
import com.cinema.booking.repositories.TicketRepository;
import com.cinema.booking.services.TicketService;
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
                .bookingId(ticket.getBooking() != null ? ticket.getBooking().getBookingId() : null)
                .movieTitle(ticket.getShowtime() != null && ticket.getShowtime().getMovie() != null
                        ? ticket.getShowtime().getMovie().getTitle() : "N/A")
                .showtimeDate(ticket.getShowtime() != null ? ticket.getShowtime().getStartTime() : null)
                .roomName(ticket.getSeat() != null && ticket.getSeat().getRoom() != null 
                        ? ticket.getSeat().getRoom().getName() : "N/A")
                .seatCode(ticket.getSeat() != null ? ticket.getSeat().getSeatCode() : null)
                .seatRow(ticket.getSeat() != null ? extractSeatRow(ticket.getSeat().getSeatCode()) : null)
                .seatNumber(ticket.getSeat() != null ? extractSeatNumber(ticket.getSeat().getSeatCode()) : null)
                .seatType(ticket.getSeat() != null && ticket.getSeat().getSeatType() != null 
                        ? ticket.getSeat().getSeatType().getName() : "STANDARD")
                .price(ticket.getUnitPrice())
                .bookingCode(ticket.getBooking() != null ? ticket.getBooking().getBookingCode() : null)
                .build();
    }

    private String extractSeatRow(String seatCode) {
        if (seatCode == null || seatCode.isBlank()) return null;
        return seatCode.substring(0, 1);
    }

    private Integer extractSeatNumber(String seatCode) {
        if (seatCode == null || seatCode.length() < 2) return null;
        try {
            return Integer.parseInt(seatCode.substring(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
