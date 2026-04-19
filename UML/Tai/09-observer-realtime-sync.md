# UML — Real-time Seat Synchronization (Observer Pattern)

> **classdiagram.md** (tham chiếu domain gốc) + **patterns/observer**

Hệ thống sử dụng **Observer Pattern** (dưới dạng Event-Driven của Spring Framework) kết hợp với **WebSocket (STOMP)** để đồng bộ trạng thái ghế theo thời gian thực giữa các quầy POS của nhân viên.

```mermaid
classDiagram
    direction TB

    %% ═══════════════════════════════════════════════════════════════════
    %% DOMAIN ENTITIES (Tham chiếu)
    %% ═══════════════════════════════════════════════════════════════════

    class Showtime {
        +Integer showtimeId
        +LocalDateTime startTime
        +Room room
    }
    class Seat {
        +Integer seatId
        +String seatCode
        +SeatType seatType
    }
    class Booking {
        +Integer bookingId
        +String bookingCode
        +BookingStatus status
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% OBSERVER PATTERN — Spring Event Driven Architecture
    %% package: patterns.observer
    %% ═══════════════════════════════════════════════════════════════════

    class ApplicationEventPublisher {
        <<interface - Spring Core>>
        +publishEvent(Object event) void
    }

    class SeatStatusEvent {
        <<Event Object>>
        +Integer showtimeId
        +Integer seatId
        +String seatCode
        +String status
        +Integer triggeredByUserId
    }

    class SeatStatusEventListener {
        <<Concrete Observer / Listener>>
        -SimpMessagingTemplate messagingTemplate
        +handleSeatStatusChange(event) void
        %% Xử lý logic: messagingTemplate.convertAndSend("/topic/seats/{id}", event)
    }

    class SimpMessagingTemplate {
        <<Infrastructure - Spring WebSocket>>
        +convertAndSend(destination, payload) void
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% SUBJECT / PUBLISHER
    %% ═══════════════════════════════════════════════════════════════════

    class BookingService {
        <<interface>>
        +lockSeat(showtimeId, seatId, userId) boolean
        +unlockSeat(showtimeId, seatId) void
        +cancelBooking(bookingId) void
        +refundBooking(bookingId) void
        +printTickets(bookingId) void
        +getSeatStatuses(showtimeId) List~SeatStatusDTO~
    }

    class BookingServiceImpl {
        <<Subject / Publisher>>
        -ApplicationEventPublisher eventPublisher
        +lockSeat(id, sid, uid) boolean
        +unlockSeat(id, sid) void
        +cancelBooking(id) void
        +refundBooking(id) void
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% RELATIONSHIPS
    %% ═══════════════════════════════════════════════════════════════════

    %% Domain Relationships
    SeatStatusEvent ..> Showtime : references ID
    SeatStatusEvent ..> Seat : references ID

    %% Observer Structure (Loose Coupling)
    BookingServiceImpl ..|> BookingService : implements
    
    %% Luồng phát sự kiện: Service tạo Event -> Đẩy qua Publisher
    BookingServiceImpl ..> SeatStatusEvent : 1. creates event instance
    BookingServiceImpl --> ApplicationEventPublisher : 2. publishes via .publishEvent()
    
    %% Luồng nhận sự kiện: Listener chỉ nhìn vào Event (Loose Coupling)
    SeatStatusEventListener ..> SeatStatusEvent : 3. receives & handles (@EventListener)
    SeatStatusEventListener --> SimpMessagingTemplate : 4. pushes to WebSocket

    %% Trigger Points
    BookingServiceImpl ..> SeatStatusEvent : 1. creates on lock/unlock
    BookingServiceImpl ..> Booking : triggers via status changes
```

### Phân tích quan hệ trong Class Diagram:

1.  **Mối quan hệ 1-N (Implicit):** `ApplicationEventPublisher` quản lý danh sách các Listener (Observers) một cách tiềm ẩn (Loose Coupling). `BookingServiceImpl` không cần biết có bao nhiêu Listener đang nghe, nó chỉ việc phát sự kiện.
2.  **Mối quan hệ Dependency (..>):** `BookingServiceImpl` phụ thuộc vào `SeatStatusEvent` để đóng gói dữ liệu trạng thái trước khi phát đi.
3.  **Mối quan hệ Association (-->):** `SeatStatusEventListener` duy trì một tham chiếu đến `SimpMessagingTemplate` để có thể đẩy dữ liệu xuống WebSocket ngay khi nhận được sự kiện.
4.  **Tính đóng gói (Encapsulation):** `SeatStatusEvent` là một Data Object bất biến (Immutable), đảm bảo dữ liệu không bị thay đổi trong quá trình truyền dẫn giữa các Observer.
