package com.cinema.booking.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Notification Publisher (Subject in Observer pattern).
 * Manages all notification observers and dispatches events to them.
 * 
 * Features:
 * - Automatically discovers all NotificationObserver beans via Spring DI
 * - Supports asynchronous notification delivery
 * - Filters observers by event type support
 * - Orders observers by priority
 * - Isolates observer failures (one observer's error doesn't affect others)
 */
@Component
@Slf4j
public class NotificationPublisher {
    
    private final List<NotificationObserver> observers;
    private final ExecutorService asyncExecutor;
    
    /**
     * Constructor injection of all NotificationObserver beans.
     * Spring automatically provides all implementations.
     */
    public NotificationPublisher(List<NotificationObserver> observers) {
        this.observers = observers;
        // Create a thread pool for async notification delivery (max 5 threads)
        this.asyncExecutor = Executors.newFixedThreadPool(5, r -> {
            Thread thread = new Thread(r);
            thread.setName("notification-worker-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
        
        log.info("NotificationPublisher initialized with {} observers: {}", 
            observers.size(), 
            observers.stream().map(NotificationObserver::getChannelName).collect(Collectors.joining(", "))
        );
    }
    
    /**
     * Publish a notification event to all interested observers synchronously.
     * Use this for critical notifications that must complete before continuing.
     * 
     * @param payload The notification payload
     */
    public void publish(NotificationPayload payload) {
        log.info("Publishing notification event: {} to {} observers", 
            payload.getEventType(), observers.size());
        
        // Filter and sort observers
        List<NotificationObserver> interestedObservers = observers.stream()
            .filter(observer -> observer.supports(payload.getEventType()))
            .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
            .collect(Collectors.toList());
        
        // Notify each observer with error isolation
        for (NotificationObserver observer : interestedObservers) {
            try {
                log.debug("Notifying observer: {} for event: {}", 
                    observer.getChannelName(), payload.getEventType());
                
                observer.handleNotification(payload);
                
                log.debug("Observer {} successfully handled event: {}", 
                    observer.getChannelName(), payload.getEventType());
                    
            } catch (Exception e) {
                // Log error but continue with other observers
                log.error("Observer {} failed to handle event {}: {}", 
                    observer.getChannelName(), payload.getEventType(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Publish a notification event asynchronously.
     * Use this for non-critical notifications that can be sent in background.
     * 
     * @param payload The notification payload
     * @return CompletableFuture for tracking async execution
     */
    public CompletableFuture<Void> publishAsync(NotificationPayload payload) {
        return CompletableFuture.runAsync(() -> {
            log.info("Publishing async notification event: {}", payload.getEventType());
            publish(payload);
        }, asyncExecutor).exceptionally(ex -> {
            log.error("Async notification failed for event {}: {}", 
                payload.getEventType(), ex.getMessage(), ex);
            return null;
        });
    }
    
    /**
     * Publish to a specific observer channel only.
     * 
     * @param channelName The channel name (e.g., "Email")
     * @param payload The notification payload
     */
    public void publishToChannel(String channelName, NotificationPayload payload) {
        observers.stream()
            .filter(observer -> observer.getChannelName().equalsIgnoreCase(channelName))
            .filter(observer -> observer.supports(payload.getEventType()))
            .findFirst()
            .ifPresent(observer -> {
                try {
                    log.info("Publishing to channel {}: {}", channelName, payload.getEventType());
                    observer.handleNotification(payload);
                } catch (Exception e) {
                    log.error("Channel {} failed to handle event {}: {}", 
                        channelName, payload.getEventType(), e.getMessage(), e);
                }
            });
    }
    
    /**
     * Shutdown the async executor gracefully.
     */
    public void shutdown() {
        if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
            log.info("NotificationPublisher async executor shutdown");
        }
    }
}
