package com.cinema.booking.services;

import com.cinema.booking.dtos.TicketDTO;
import java.util.List;

public interface TicketService {
    List<TicketDTO> getTicketsByBooking(Integer bookingId);
    List<TicketDTO> getTicketsByUser(Integer userId);
    TicketDTO getTicketDetails(Integer ticketId);
    void deleteTicket(Integer ticketId);
}
