```mermaid
classDiagram
    class User {
        <<abstract>>
        -int user_ID
        -String fullname
        -String phone
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
        -int id
        -String email
        -String password_hash
        +login()
        +changePassword()
        +resetPassword()
    }

    class Location {
        -int id
        -String name
        +getLocations()
    }

    class Cinema {
        -int id
        -String name
        -String address
        +getDetails()
    }

    class Room {
        -int id
        -String name
        -List~Seat~ seatList
        -String screen_type
        +getCapacity()
    }

    class Seat {
        -int id
        -String seat_code
    }

    class SeatType {
        -int seat_ID
        -String name
        -float price_surcharge
    }

    class Movie {
        -int movie_ID
        -String title
        -String description
        -int duration_minutes
        -Date release_date
        -String language
        -String status
        -List~MovieCast~ movieCastList
        +getDetails()
    }

    class Genre {
        -int id
        -String name
    }

    class CastMember {
        -int cast_memberID
        -String full_name
        -String bio
        -Date birth_date
        -String nationality
    }

    class MovieCast {
        -CastMember castMember
        -String role_name
        -String role_type
    }

    class Showtime {
        -int showtime_ID
        -Date start_time
        -Date end_time
        -int basePrice
        +getAvailableTickets()
    }

    class FnbItem {
        -int FnbItem_ID
        -String name
        -String description
        -float price
    }

    class Promotion {
        -int id
        -String code
        -String discount_type
        -float discount_value
        -Date valid_to
    }

    class Booking {
        -int id
        -int userID
        -String booking_code
        -String status
        -Date created_at
        -List~Ticket~ TicketList
        +calculateTotals()
        +applyPromotion()
    }

    class Ticket {
        -int id
        -int movie_ID
        -int showtime_ID
        -int seat_ID
        -float unit_price
        -Date hold_expires_at
        +printTicket()
    }

    class FnbLine {
        -id
        -int FnbItem_ID
        -int quantity
        +calculateLineTotal()
    }

    class Payment {
        -int id
        -String payment_method
        -float amount
        -Date paid_at
        -String status
        +processPayment()
    }

    class Notification {
        -int notification_ID
        -String title
        -String message
        +markasRead()
    }

    %% Relationships
    UserAccount "1" -- "1" User : has
    User <|-- Customer : Extends
    User <|-- Admin : Extends
    User <|-- Staff : Extends
    User "1" -- "n" Notification : receives

    Cinema "n" -- "1" Location : located_in
    Cinema "1" -- "n" Room : contains
    Room "1" -- "n" Showtime : hosts
    Movie "1" -- "n" Showtime : scheduled_as

    Movie "*" -- "*" Genre : has

    CastMember "1" o-- "1" MovieCast : aggregation
    Movie "1" *-- "n" MovieCast : contains (Composition)

    Room "1" *-- "n" Seat : contains (Composition)
    Seat "n" -- "1" SeatType : has_type

    Booking "1" -- "0..1" Promotion : uses
    Booking "1" -- "1" Payment : has
    Booking "1" -- "0..n" FnbLine : includes
    Booking "1" o-- "n" Ticket : aggregation

    FnbLine "n" -- "1" FnbItem : refers_to


```

UML tung design pattern (GoF) **khong** gop vao file nay — moi pattern co `classDiagram` rieng trong [plans/01](plans/01-chain-of-responsibility-checkout-validation.md) … [plans/07](plans/07-prototype-email-templates.md) va quy uoc [plans/00](plans/00-patterns-conventions.md).

**Sau khi ap dung pattern:** moi pattern van co **UML rieng** trong `plans/01` … `plans/07` — **khong** gop nhieu pattern vao mot diagram hay gop pattern sau vao diagram pattern truoc. Chi muc: [uml-patterns-index.md](uml-patterns-index.md).