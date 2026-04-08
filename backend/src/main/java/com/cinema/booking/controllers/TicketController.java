package com.cinema.booking.controllers;

import com.cinema.booking.dtos.TicketDTO;
import com.cinema.booking.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "7. Quản lý Vé (Tickets)", description = "Các API truy vấn thông tin vé đã đặt")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Operation(summary = "Lấy danh sách vé theo Booking ID")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByBooking(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(ticketService.getTicketsByBooking(bookingId));
    }

    @Operation(summary = "Lấy danh sách vé của người dùng")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUser(userId));
    }

    @Operation(summary = "Lấy thông tin chi tiết của một vé")
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDTO> getTicketDetails(@PathVariable Integer ticketId) {
        try {
            return ResponseEntity.ok(ticketService.getTicketDetails(ticketId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Xóa vé (Dành cho nhân viên soát vé)")
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Integer ticketId) {
        try {
            ticketService.deleteTicket(ticketId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
