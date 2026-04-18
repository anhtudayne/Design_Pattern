package com.cinema.booking.pattern.observer;
import com.cinema.booking.pattern.observer.NotificationPayload;
import com.cinema.booking.pattern.observer.NotificationEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Payload carrier for notification events.
 * Contains all contextual data needed by observers to process notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    
    /** The type of notification event */
    private NotificationEvent eventType;
    
    /** Primary entity ID (e.g., bookingId, userId) */
    private Long primaryId;
    
    /** Secondary entity ID if needed (e.g., paymentId) */
    private Long secondaryId;
    
    /** Recipient email address */
    private String email;
    
    /** Recipient full name */
    private String recipientName;
    
    /** Additional context data as key-value pairs */
    @Builder.Default
    private Map<String, Object> contextData = new HashMap<>();
    
    /**
     * Helper method to add context data
     */
    public NotificationPayload addContextData(String key, Object value) {
        this.contextData.put(key, value);
        return this;
    }
    
    /**
     * Helper method to get typed context data
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextData(String key, Class<T> type) {
        Object value = contextData.get(key);
        return value != null ? (T) value : null;
    }
}
