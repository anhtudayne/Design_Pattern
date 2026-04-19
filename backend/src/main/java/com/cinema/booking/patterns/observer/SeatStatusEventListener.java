package com.cinema.booking.patterns.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Spring Event Listener (Concrete Observer).
 * 
 * Đây là cách tiếp cận "Spring-way" cho Observer Pattern. 
 * Thay vì tự quản lý danh sách Observer, ta tận dụng ApplicationEventPublisher của Spring.
 * 
 * Component này lắng nghe mọi SeatStatusEvent được phát ra từ hệ thống
 * và đẩy kết quả xuống các quầy POS thông qua WebSocket.
 */
@Component
@Slf4j
public class SeatStatusEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SeatStatusEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * @EventListener: Đăng ký phương thức này làm một Observer cho SeatStatusEvent.
     */
    @EventListener
    public void handleSeatStatusChange(SeatStatusEvent event) {
        String topic = "/topic/seats/" + event.getShowtimeId();
        try {
            messagingTemplate.convertAndSend(topic, event);
            log.info("[Observer] Real-time Sync → Topic: {}, Seat: {}, Status: {}", 
                    topic, event.getSeatCode() != null ? event.getSeatCode() : event.getSeatId(), event.getStatus());
        } catch (Exception ex) {
            log.error("[Observer] Lỗi khi đẩy dữ liệu WebSocket: {}", ex.getMessage());
        }
    }
}
