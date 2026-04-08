package com.cinema.booking.services;

public interface EmailService {
    void sendTicketEmail(Integer bookingId);
    void sendWelcomeEmail(String email, String fullname);
}
