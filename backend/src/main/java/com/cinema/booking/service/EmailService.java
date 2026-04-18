package com.cinema.booking.service;

public interface EmailService {
    void sendTicketEmail(Integer bookingId);
    void sendWelcomeEmail(String email, String fullname);
}
