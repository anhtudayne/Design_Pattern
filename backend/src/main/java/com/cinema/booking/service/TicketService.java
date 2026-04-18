package com.cinema.booking.service;

import com.cinema.booking.dto.TicketDTO;
import java.util.List;

public interface TicketService {
    List<TicketDTO> getTicketsByBooking(Integer bookingId);
    List<TicketDTO> getTicketsByUser(Integer userId);
    TicketDTO getTicketDetails(Integer ticketId);
    void deleteTicket(Integer ticketId);
}
