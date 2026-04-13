# Luồng ghế — trạng thái & khóa tạm (Redis)

**Áp dụng:** **State** (VACANT / PENDING / SOLD từ snapshot DB + lock) + **Adapter** (`SeatLockProvider` ← `RedisSeatLockAdapter` bọc Redis).

---

## 1. Class diagram — State + Adapter

```mermaid
classDiagram
direction TB

class SeatState {
  <<interface>>
  +toDisplayStatus() SeatStatus
  +allowsLockAttempt() boolean
}

class VacantSeatState {
  +INSTANCE
}

class PendingSeatState {
  +INSTANCE
}

class SoldSeatState {
  +INSTANCE
}

SeatState <|.. VacantSeatState
SeatState <|.. PendingSeatState
SeatState <|.. SoldSeatState

class SeatStateFactory {
  <<utility>>
  +fromSnapshot(soldInDb, redisLockPresent) SeatState
}

SeatStateFactory ..> SeatState : resolves

class SeatLockProvider {
  <<interface>>
  +tryAcquire(showtimeId, seatId, userId, ttlSec) boolean
  +release(showtimeId, seatId) void
  +batchLockHeld(showtimeId, seatIds) List
}

class RedisSeatLockAdapter {
  -redisTemplate: RedisTemplate
  +tryAcquire(...) boolean
  +release(...) void
  +batchLockHeld(...) List
}

SeatLockProvider <|.. RedisSeatLockAdapter

class BookingServiceImpl {
  -seatLockProvider: SeatLockProvider
  +getSeatStatuses(showtimeId) List
  +lockSeat(showtimeId, seatId, userId) boolean
  +unlockSeat(showtimeId, seatId) void
}

BookingServiceImpl --> SeatLockProvider
BookingServiceImpl ..> SeatStateFactory : uses
BookingServiceImpl ..> SeatState : uses
```

---

## 2. Sequence diagram — lấy sơ đồ ghế (`getSeatStatuses`)

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant BookingController
    participant BookingServiceImpl
    participant TicketRepository as TicketRepo_DB
    participant SeatLockProvider
    participant RedisSeatLockAdapter
    participant Redis
    participant SeatStateFactory
    participant SeatState

    Client->>BookingController: GET /booking/seats/{showtimeId}
    BookingController->>BookingServiceImpl: getSeatStatuses(showtimeId)
    BookingServiceImpl->>BookingServiceImpl: load seats, showtime
    BookingServiceImpl->>TicketRepo_DB: vé đã bán theo suất
    TicketRepo_DB-->>BookingServiceImpl: soldSeatIds
    BookingServiceImpl->>SeatLockProvider: batchLockHeld(showtimeId, seatIds)
    SeatLockProvider->>RedisSeatLockAdapter: batchLockHeld(...)
    RedisSeatLockAdapter->>Redis: MGET lock keys
    Redis-->>RedisSeatLockAdapter: values
    RedisSeatLockAdapter-->>BookingServiceImpl: danh sách held theo ghế

    loop mỗi ghế
        BookingServiceImpl->>SeatStateFactory: fromSnapshot(sold, held)
        SeatStateFactory-->>BookingServiceImpl: SeatState instance
        BookingServiceImpl->>SeatState: toDisplayStatus()
        SeatState-->>BookingServiceImpl: VACANT | PENDING | SOLD
    end

    BookingServiceImpl-->>BookingController: List SeatStatusDTO
    BookingController-->>Client: JSON
```

---

## 3. Sequence diagram — khóa / mở khóa ghế

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant BookingController
    participant BookingServiceImpl
    participant TicketRepository as TicketRepo_DB
    participant SeatStateFactory
    participant SeatLockProvider
    participant RedisSeatLockAdapter
    participant Redis

    Note over Client,Redis: lockSeat
    Client->>BookingController: POST /booking/lock
    BookingController->>BookingServiceImpl: lockSeat(showtimeId, seatId, userId)
    BookingServiceImpl->>TicketRepo_DB: đã có vé cho ghế?
    TicketRepo_DB-->>BookingServiceImpl: isSold
    BookingServiceImpl->>SeatStateFactory: fromSnapshot(isSold, false)
    SeatStateFactory-->>BookingServiceImpl: SeatState
    BookingServiceImpl->>BookingServiceImpl: allowsLockAttempt()?
    alt đã bán (SoldSeatState)
        BookingServiceImpl-->>BookingController: false
    else còn có thể thử lock
        BookingServiceImpl->>SeatLockProvider: tryAcquire(..., ttl)
        SeatLockProvider->>RedisSeatLockAdapter: tryAcquire
        RedisSeatLockAdapter->>Redis: SETNX + EXPIRE
        Redis-->>RedisSeatLockAdapter: OK / null
        RedisSeatLockAdapter-->>BookingServiceImpl: boolean
    end
    BookingController-->>Client: 200 / 400

    Note over Client,Redis: unlockSeat
    Client->>BookingController: POST /booking/unlock
    BookingController->>BookingServiceImpl: unlockSeat(showtimeId, seatId)
    BookingServiceImpl->>SeatLockProvider: release(showtimeId, seatId)
    SeatLockProvider->>RedisSeatLockAdapter: release
    RedisSeatLockAdapter->>Redis: DEL key
    BookingController-->>Client: 200
```
