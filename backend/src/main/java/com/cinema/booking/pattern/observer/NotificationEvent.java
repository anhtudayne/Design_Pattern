package com.cinema.booking.pattern.observer;
import com.cinema.booking.pattern.observer.NotificationEvent;

/**
 * Enum defining all notification event types in the system.
 * Each event type triggers different notification handling logic.
 */
public enum NotificationEvent {
    
    /** Triggered when a new user account is created */
    USER_REGISTERED,
    
    /** Triggered when a booking is confirmed (payment successful) */
    BOOKING_CONFIRMED,
    
    /** Triggered when a booking is cancelled */
    BOOKING_CANCELLED,
    
    /** Triggered when a booking is refunded */
    BOOKING_REFUNDED,
    
    /** Triggered when payment fails */
    PAYMENT_FAILED,
    
    /** Triggered when a booking is about to expire (e.g., 1 hour before showtime) */
    BOOKING_REMINDER,
    
    /** Triggered when promotional campaigns are launched */
    PROMOTION_AVAILABLE
}
