# UML — Chain of Responsibility + Domain

> **classdiagram.md** (đầy đủ) + **plans/01** Chain of Responsibility pattern.

```mermaid
classDiagram
  direction TB

  %% ═══════════════════════════════════════════════════════════
  %% DOMAIN — classdiagram.md (toàn bộ, không cắt bớt)
  %% ═══════════════════════════════════════════════════════════

  class User {
    <<abstract>>
    -id: int
    -fullname: String
    -phone: String
    +updateProfile()
  }
  class Customer {
    +bookTicket()
    +writeReview()
  }
  class Admin {
    +manageUsers()
    +manageCinemas()
    +viewSystemReports()
    +viewDashboard()
  }
  class Staff {
    +sellTicketOffline()
    +manageFnbOrders()
  }
  class UserAccount {
    -id: int
    -email: String
    -password_hash: String
    -created_at: Date
    +login()
    +changePassword()
    +resetPassword()
  }
  class Notification {
    -id: int
    -title: String
    -message: String
    -created_at: Date
    +markAsRead()
  }
  class Location {
    -id: int
    -name: String
    +getLocations()
  }
  class Cinema {
    -id: int
    -name: String
    -address: String
    -hotline: String
    +getDetails()
  }
  class Room {
    -id: int
    -name: String
    -screen_type: String
    +getCapacity()
  }
  class Seat {
    -id: int
    -seat_code: String
    -is_active: boolean
  }
  class SeatType {
    -id: int
    -name: String
    -price_surcharge: float
  }
  class Movie {
    -id: int
    -title: String
    -description: String
    -duration_minutes: int
    -release_date: Date
    -language: String
    -poster_url: String
    -trailer_url: String
    -status: String
    +getDetails()
  }
  class Genre {
    -id: int
    -name: String
  }
  class CastMember {
    -id: int
    -full_name: String
    -bio: String
    -birth_date: Date
    -nationality: String
    -image_url: String
  }
  class MovieCast {
    -role_name: String
    -role_type: String
  }
  class Review {
    -id: int
    -rating_stars: int
    -comment: String
    -created_at: Date
  }
  class Showtime {
    -id: int
    -start_time: Date
    -end_time: Date
    +getAvailableTickets()
  }
  class Ticket {
    -id: int
    -unit_price: float
    -status: String
    -hold_expires_at: Date
    +printTicket()
  }
  class Booking {
    -id: int
    -booking_code: String
    -status: String
    -created_at: Date
    +calculateTotals()
    +applyPromotion()
  }
  class Payment {
    -id: int
    -payment_method: String
    -amount: float
    -status: String
    -paid_at: Date
    +processPayment()
  }
  class FnbItem {
    -id: int
    -name: String
    -description: String
    -price: float
    -stock_quantity: int
    -image_url: String
    -is_active: boolean
  }
  class FnbLine {
    -id: int
    -unit_price: int
    -quantity: int
    +calculateLineTotal()
  }
  class Promotion {
    -id: int
    -code: String
    -discount_type: String
    -discount_value: float
    -valid_to: Date
    +deductQuantity()
  }
  class PromotionInventory {
    -id: int
    -quantity: int
    +checkAvailable()
  }

  %% Domain relationships
  User <|-- Customer : Extends
  User <|-- Admin : Extends
  User <|-- Staff : Extends
  UserAccount "1" -- "1" User
  User "1" --> "*" Notification
  Location "1" -- "*" Cinema
  Cinema "1" o-- "*" Room
  Room "1" *-- "*" Seat
  Seat "*" --> "1" SeatType
  Movie "*" -- "*" Genre
  Movie "1" o-- "*" MovieCast
  MovieCast "*" -- "1" CastMember
  Movie "1" -- "*" Review
  Customer "1" -- "*" Review
  Movie "1" -- "*" Showtime
  Room "1" -- "*" Showtime
  Showtime "1" -- "*" Ticket
  Seat "1" -- "*" Ticket
  Customer "1" -- "*" Booking
  Booking "1" o-- "*" Ticket
  Booking "1" -- "1" Payment
  Booking "1" -- "*" FnbLine
  FnbLine "*" --o "1" FnbItem
  Booking "*" -- "0..1" Promotion
  Promotion "1" -- "*" PromotionInventory

  %% ═══════════════════════════════════════════════════════════
  %% PATTERN — Chain of Responsibility (plans/01)
  %% ═══════════════════════════════════════════════════════════

  class CheckoutValidationContext {
    -userId: Integer
    -showtimeId: Integer
    -seatIds: List~Integer~
    -promoCode: String
    -user: User
    -showtime: Showtime
  }
  class CheckoutValidationHandler {
    <<interface>>
    +setNext(handler)
    +handle(ctx)
  }
  class AbstractCheckoutValidationHandler {
    <<abstract>>
    -next: CheckoutValidationHandler
    +handle(ctx)
    #doHandle(ctx)*
  }
  class MaxSeatsHandler
  class UserExistsHandler
  class ShowtimeExistsHandler
  class SeatsNotSoldHandler
  class CheckoutValidationConfig {
    <<configuration>>
  }

  %% Pattern relationships
  CheckoutValidationHandler <|.. AbstractCheckoutValidationHandler
  AbstractCheckoutValidationHandler <|-- MaxSeatsHandler
  AbstractCheckoutValidationHandler <|-- UserExistsHandler
  AbstractCheckoutValidationHandler <|-- ShowtimeExistsHandler
  AbstractCheckoutValidationHandler <|-- SeatsNotSoldHandler
  AbstractCheckoutValidationHandler o-- CheckoutValidationHandler : next
  CheckoutValidationConfig ..> CheckoutValidationHandler : builds chain
  CheckoutValidationHandler ..> CheckoutValidationContext : uses

  %% Domain ↔ Pattern connections
  CheckoutValidationContext --> User : cached user
  CheckoutValidationContext --> Showtime : cached showtime
  CheckoutServiceImpl ..> CheckoutValidationHandler : uses chain
  CheckoutServiceImpl ..> Booking : creates
```
