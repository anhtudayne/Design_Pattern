package com.cinema.booking.services.notification.observers;

import com.cinema.booking.services.notification.NotificationEvent;
import com.cinema.booking.services.notification.NotificationObserver;
import com.cinema.booking.services.notification.NotificationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SMS notification observer (Placeholder for future implementation).
 * Demonstrates how easy it is to add new notification channels
 * without modifying existing code (Open-Closed Principle).
 */
@Component
@Slf4j
public class SmsNotificationObserver implements NotificationObserver {
    
    @Override
    public void handleNotification(NotificationPayload payload) {
        log.info("SMS notification triggered for event: {}", payload.getEventType());
        
        switch (payload.getEventType()) {
            case BOOKING_CONFIRMED:
                sendBookingConfirmationSms(payload);
                break;
                
            case BOOKING_REMINDER:
                sendBookingReminderSms(payload);
                break;
                
            case PAYMENT_FAILED:
                sendPaymentFailedSms(payload);
                break;
                
            default:
                log.debug("SMS observer ignoring event: {}", payload.getEventType());
        }
    }
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
    
    @Override
    public boolean supports(NotificationEvent eventType) {
        // SMS only supports critical events
        return eventType == NotificationEvent.BOOKING_CONFIRMED 
            || eventType == NotificationEvent.BOOKING_REMINDER
            || eventType == NotificationEvent.PAYMENT_FAILED;
    }
    
    @Override
    public int getPriority() {
        return 50; // Medium priority
    }
    
    private void sendBookingConfirmationSms(NotificationPayload payload) {
        // TODO: Integrate with SMS provider (Twilio, AWS SNS, etc.)
        log.info("Would send SMS to {}: Booking #{} confirmed", 
            payload.getRecipientName(), payload.getPrimaryId());
    }
    
    private void sendBookingReminderSms(NotificationPayload payload) {
        // TODO: Integrate with SMS provider
        log.info("Would send reminder SMS to {}: Showtime approaching for booking #{}", 
            payload.getRecipientName(), payload.getPrimaryId());
    }
    
    private void sendPaymentFailedSms(NotificationPayload payload) {
        // TODO: Integrate with SMS provider
        log.warn("Would send payment failure SMS to {}: Booking #{}", 
            payload.getRecipientName(), payload.getPrimaryId());
    }
}
