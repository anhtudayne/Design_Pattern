```mermaid
classDiagram
    class User {
        <<abstract>>
        -id: int
        -fullname: String
        -phone: String
        -created_at: DateTime
    }

    class Customer {
        -total_spending: decimal
        -loyalty_points: int
    }

    class MembershipTier {
        -id: int
        -name: String
        -min_spending: decimal
        -discount_percent: decimal
    }

    class Admin
    class Staff

    class UserAccount {
        -id: int
        -email: String
        -password_hash: String
    }

    class Notification {
        -id: int
        -title: String
        -message: String
        -is_read: boolean
    }

    class Location {
        -id: int
        -name: String
    }

    class Cinema {
        -id: int
        -name: String
        -address: String
    }

    class Room {
        -id: int
        -name: String
        -screen_type: String
    }

    class Seat {
        -id: int
        -seat_code: String
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
        -age_rating: String
        -poster_url: String
        -trailer_url: String
        -status: String
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
        -rating: int
        -comment: String
    }

    class Showtime {
        -id: int
        -start_time: DateTime
        -end_time: DateTime
        -base_price: decimal
    }

    class Ticket {
        -id: int
        -price: decimal
    }

    class Booking {
        -id: int
        -booking_code: String
        -status: String
        -created_at: DateTime
    }

    class Payment {
        -id: int
        -method: String
        -amount: decimal
        -status: String
        -paid_at: DateTime
    }

    class FnbItem {
        -id: int
        -name: String
        -description: String
        -price: float
        -image_url: String
        -is_active: boolean
    }

    class FnbItemInventory {
        -id: int
        -quantity: int
        -version: long
    }

    class FnbLine {
        -id: int
        -unit_price: decimal
        -quantity: int
    }

    class Promotion {
        -id: int
        -code: String
        -discount_type: String
        -discount_value: decimal
        -valid_to: DateTime
    }

    class PromotionInventory {
        -id: int
        -quantity: int
        -version: long
    }

    %% Relationships
    User <|-- Customer : Extends
    User <|-- Admin : Extends
    User <|-- Staff : Extends
    UserAccount "1" -- "1" User
    Customer "*" --> "0..1" MembershipTier
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
    FnbLine "*" --> "1" FnbItem
    FnbItem "1" -- "1" FnbItemInventory
    
    Booking "*" --> "0..1" Promotion
    Promotion "1" -- "1" PromotionInventory
```

UML tung design pattern (GoF) **khong** gop vao file nay — moi pattern co `classDiagram` rieng trong [plans/01](plans/01-chain-of-responsibility-checkout-validation.md) … [plans/07](plans/07-prototype-email-templates.md) va quy uoc [plans/00](plans/00-patterns-conventions.md).

**Sau khi ap dung pattern:** moi pattern van co **UML rieng** trong `plans/01` … `plans/07` — **khong** gop nhieu pattern vao mot diagram hay gop pattern sau vao diagram pattern truoc. Chi muc: [uml-patterns-index.md](uml-patterns-index.md).