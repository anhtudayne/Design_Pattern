package com.cinema.booking.services.notification;

/**
 * Observer interface for the Notification system.
 * All notification channels (Email, SMS, Push, etc.) must implement this interface.
 * 
 * This follows the Observer pattern from GoF Design Patterns.
 */
public interface NotificationObserver {
    
    /**
     * Handle a notification event
     * 
     * @param payload The notification payload containing event data
     */
    void handleNotification(NotificationPayload payload);
    
    /**
     * Get the notification channel name (for logging/debugging)
     * 
     * @return Channel name (e.g., "Email", "SMS", "Push Notification")
     */
    String getChannelName();
    
    /**
     * Check if this observer supports the given event type
     * Default implementation returns true for all events.
     * Override to filter specific event types.
     * 
     * @param eventType The notification event type
     * @return true if this observer handles this event type
     */
    default boolean supports(NotificationEvent eventType) {
        return true;
    }
    
    /**
     * Get the priority of this observer (lower number = higher priority)
     * Default is 100. Override to control execution order.
     * 
     * @return Priority value
     */
    default int getPriority() {
        return 100;
    }
}
