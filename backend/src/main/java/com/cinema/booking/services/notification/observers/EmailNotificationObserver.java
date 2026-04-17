package com.cinema.booking.services.notification.observers;

import com.cinema.booking.services.EmailService;
import com.cinema.booking.services.notification.NotificationEvent;
import com.cinema.booking.services.notification.NotificationObserver;
import com.cinema.booking.services.notification.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email notification observer.
 * Handles all email-based notifications triggered by various events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationObserver implements NotificationObserver {
    
    private final EmailService emailService;
    
    @Override
    public void handleNotification(NotificationPayload payload) {
        log.info("Email notification triggered for event: {}", payload.getEventType());
        
        try {
            switch (payload.getEventType()) {
                case USER_REGISTERED:
                    handleUserRegistered(payload);
                    break;
                    
                case BOOKING_CONFIRMED:
                    handleBookingConfirmed(payload);
                    break;
                    
                case BOOKING_CANCELLED:
                    handleBookingCancelled(payload);
                    break;
                    
                case BOOKING_REFUNDED:
                    handleBookingRefunded(payload);
                    break;
                    
                case PAYMENT_FAILED:
                    handlePaymentFailed(payload);
                    break;
                    
                default:
                    log.debug("Email observer ignoring event: {}", payload.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to send email notification for {}: {}", 
                payload.getEventType(), e.getMessage(), e);
            throw new RuntimeException("Email notification failed", e);
        }
    }
    
    @Override
    public String getChannelName() {
        return "Email";
    }
    
    @Override
    public boolean supports(NotificationEvent eventType) {
        // Email supports all event types
        return true;
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority for email notifications
    }
    
    private void handleUserRegistered(NotificationPayload payload) {
        log.info("Sending welcome email to: {}", payload.getEmail());
        emailService.sendWelcomeEmail(payload.getEmail(), payload.getRecipientName());
    }
    
    private void handleBookingConfirmed(NotificationPayload payload) {
        Long bookingId = payload.getPrimaryId();
        log.info("Sending ticket email for booking: {}", bookingId);
        emailService.sendTicketEmail(bookingId.intValue());
    }
    
    private void handleBookingCancelled(NotificationPayload payload) {
        // TODO: Implement cancellation email
        log.info("Booking cancelled notification for booking ID: {}", payload.getPrimaryId());
        // emailService.sendCancellationEmail(payload.getPrimaryId().intValue());
    }
    
    private void handleBookingRefunded(NotificationPayload payload) {
        // TODO: Implement refund email
        log.info("Booking refunded notification for booking ID: {}", payload.getPrimaryId());
        // emailService.sendRefundEmail(payload.getPrimaryId().intValue());
    }
    
    private void handlePaymentFailed(NotificationPayload payload) {
        // TODO: Implement payment failure email
        log.warn("Payment failed notification for booking ID: {}", payload.getPrimaryId());
        // emailService.sendPaymentFailedEmail(payload.getPrimaryId().intValue());
    }
}
