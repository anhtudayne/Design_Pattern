# UML — Dynamic Pricing Engine (domain subset + 5 patterns)

> **classdiagram.md** (tham chiếu domain gốc) + **docs/patterns/08-dynamic-pricing-engine.md**

Tham chiếu domain đầy đủ: [classdiagram.md](../classdiagram.md).  
Tài liệu giải thích: [08-dynamic-pricing-engine.md](../docs/patterns/08-dynamic-pricing-engine.md).



```mermaid
classDiagram
    direction TB

    %% ═══════════════════════════════════════════════════════════════════
    %% DOMAIN ENTITIES
    %% ═══════════════════════════════════════════════════════════════════

    class Showtime {
        +Integer showtimeId
        +LocalDateTime startTime
        +BigDecimal basePrice
        +Room room
    }
    class Seat {
        +Integer seatId
        +String seatCode
        +SeatType seatType
    }
    class SeatType {
        +String name
        +BigDecimal priceSurcharge
    }
    class Customer {
        +Integer userId
        +String fullname
        +MembershipTier tier
    }
    class MembershipTier {
        +String name
        +BigDecimal discountPercent
    }
    class Promotion {
        +String code
        +String discountType
        +BigDecimal discountValue
        +Integer quantity
        +LocalDateTime validTo
    }
    class FnbItem {
        +Integer itemId
        +String name
        +BigDecimal price
    }
    class BookingCalculationDTO {
        +Integer showtimeId
        +List~Integer~ seatIds
        +String promoCode
    }
    class PriceBreakdownDTO {
        +BigDecimal ticketTotal
        +BigDecimal timeBasedSurcharge
        +BigDecimal fnbTotal
        +BigDecimal membershipDiscount
        +BigDecimal discountAmount
        +BigDecimal finalTotal
        +String appliedStrategy
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% CHAIN OF RESPONSIBILITY — pricing validation
    %% package: services.strategy_decorator.pricing.validation
    %% ═══════════════════════════════════════════════════════════════════

    class PricingValidationHandler {
        <<interface>>
        +setNext(next) void
        +validate(ctx) void
    }

    class AbstractPricingValidationHandler {
        <<abstract>>
        -next: PricingValidationHandler
        +validate(ctx) void
        #doValidate(ctx) void
    }

    class PricingValidationContext {
        +BookingCalculationDTO request
        +Showtime showtime
        +Promotion promotion
        %% showtime: ShowtimeFutureHandler MUST run first (builder reuses it, avoids second DB load)
        %% promotion: PromoValidHandler sets from request.promoCode only (does not read showtime from ctx)
    }

    class ShowtimeFutureHandler {
        +doValidate(ctx) void
    }

    class SeatsAvailableHandler {
        +doValidate(ctx) void
    }

    class PromoValidHandler {
        +doValidate(ctx) void
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% PROXY — Redis cache
    %% package: services.strategy_decorator.pricing
    %% ═══════════════════════════════════════════════════════════════════

    class IPricingEngine {
        <<interface>>
        +calculateTotalPrice(ctx) PriceBreakdownDTO
    }

    class CachingPricingEngineProxy {
        <<Primary>>
        -delegate: IPricingEngine
        -redisTemplate: RedisTemplate
        -ttlSeconds: long
        +calculateTotalPrice(ctx) PriceBreakdownDTO
        -buildCacheKey(ctx) String
        %% Cache key = pricing:{showtimeId}:seats:{sortedIds}:fnb:{sorted itemId:qty}:promo:{code}:cust:{id}
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% SPECIFICATION — PricingConditions
    %% package: patterns.specification
    %% ═══════════════════════════════════════════════════════════════════

    class PricingSpecificationContext {
        <<patterns.specification>>
        +Showtime showtime
        +List~Seat~ seats
        +Customer customer
        +Promotion promotion
        +BigDecimal fnbTotal
        +int bookedSeatsCount
        +int totalSeatsCount
        +LocalDateTime bookingTime
    }

    class PricingConditions {
        <<utility>>
        +isWeekend()$ Predicate
        +isHoliday()$ Predicate
        +isEarlyBird()$ Predicate
        +isHighOccupancy(pct)$ Predicate
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% ENGINE CONTEXT
    %% package: services.strategy_decorator.pricing
    %% ═══════════════════════════════════════════════════════════════════

    class PricingContext {
        <<Builder>>
        +Showtime showtime
        +List~Seat~ seats
        +List~ResolvedFnbItem~ resolvedFnbs
        +Promotion promotion
        +Customer customer
        +LocalDateTime bookingTime
        +int bookedSeatsCount
        +int totalSeatsCount
    }

    class ResolvedFnbItem {
        <<record>>
        +Integer itemId
        +String name
        +BigDecimal price
        +int quantity
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% STRATEGY — pricing
    %% ═══════════════════════════════════════════════════════════════════

    class PricingLineType {
        <<enumeration>>
        TICKET
        FNB
        TIME_BASED_SURCHARGE
    }

    class PricingStrategy {
        <<interface>>
        +lineType() PricingLineType
        +calculate(ctx) BigDecimal
    }

    class TicketPricingStrategy {
        +lineType() PricingLineType
        +calculate(ctx) BigDecimal
    }

    class FnbPricingStrategy {
        +lineType() PricingLineType
        +calculate(ctx) BigDecimal
    }

    class TimeBasedPricingStrategy {
        -weekendSurchargePct: BigDecimal
        -holidaySurchargePct: BigDecimal
        +lineType() PricingLineType
        +calculate(ctx) BigDecimal
        %% Returns timeBasedSurcharge = ticketSubtotal × rate (weekend / holiday)
        -toSpecContext(ctx) PricingSpecificationContext

    %% ═══════════════════════════════════════════════════════════════════
    %% DECORATOR — discount chain
    %% ═══════════════════════════════════════════════════════════════════

    class DiscountComponent {
        <<interface>>
        +applyDiscount(subtotal, ctx) DiscountResult
    }

    class BaseDiscountDecorator {
        <<abstract>>
        #wrapped: DiscountComponent
        +applyDiscount(subtotal, ctx) DiscountResult
    }

    class NoDiscount {
        +applyDiscount(subtotal, ctx) DiscountResult
    }

    class PromotionDiscountDecorator {
        +applyDiscount(subtotal, ctx) DiscountResult
    }

    class MemberDiscountDecorator {
        +applyDiscount(subtotal, ctx) DiscountResult
    }

    class DiscountResult {
        +BigDecimal totalDiscount
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% ORCHESTRATOR
    %% ═══════════════════════════════════════════════════════════════════

    class PricingEngine {
        <<component pricingEngine>>
        -strategiesByLine: EnumMap~PricingLineType,PricingStrategy~
        %% Built from injected List~PricingStrategy~ unique lineType per enum value
        -noDiscount: NoDiscount
        +calculateTotalPrice(ctx) PriceBreakdownDTO
        -buildDiscountChain(ctx) DiscountComponent
    }

    class PricingContextBuilder {
        <<Component>>
        +build(validationCtx, request) PricingContext
        %% After CoR: loads seats, FnB prices, customer, occupancy → PricingContext
    }

    class BookingServiceImpl {
        <<service>>
        -pricingEngine: IPricingEngine
        -pricingValidationChain: PricingValidationHandler
        -pricingContextBuilder: PricingContextBuilder
        +calculatePrice(dto) PriceBreakdownDTO
    }

    %% ═══════════════════════════════════════════════════════════════════
    %% RELATIONSHIPS
    %% ═══════════════════════════════════════════════════════════════════

    %% Domain
    Seat --> SeatType : has
    Customer --> MembershipTier : has
    PricingContext o-- ResolvedFnbItem : contains
    PricingContext --> Showtime : uses
    PricingContext --> Seat : uses
    PricingContext --> Customer : uses
    PricingContext --> Promotion : uses
    ResolvedFnbItem ..> FnbItem : resolved from

    %% Input / Output
    BookingServiceImpl ..> BookingCalculationDTO : receives
    BookingServiceImpl ..> PriceBreakdownDTO : returns
    PricingValidationContext --> BookingCalculationDTO : wraps
    PricingValidationContext --> Showtime : populated by handler
    PricingValidationContext --> Promotion : populated by handler

    %% CoR chain
    PricingValidationHandler <|.. AbstractPricingValidationHandler
    AbstractPricingValidationHandler <|-- ShowtimeFutureHandler
    AbstractPricingValidationHandler <|-- SeatsAvailableHandler
    AbstractPricingValidationHandler <|-- PromoValidHandler
    AbstractPricingValidationHandler o-- PricingValidationHandler : next
    ShowtimeFutureHandler ..> PricingValidationContext : populates showtime
    PromoValidHandler ..> PricingValidationContext : populates promotion

    %% Proxy
    IPricingEngine <|.. PricingEngine
    IPricingEngine <|.. CachingPricingEngineProxy
    CachingPricingEngineProxy o-- IPricingEngine : delegate

    %% Specification
    TimeBasedPricingStrategy ..> PricingConditions : evaluates via
    TimeBasedPricingStrategy ..> PricingSpecificationContext : converts to
    PricingConditions ..> PricingSpecificationContext : tests
    PricingSpecificationContext --> Showtime : references
    PricingSpecificationContext --> Customer : references
    PricingSpecificationContext --> Promotion : references

    %% Strategy
    PricingStrategy <|.. TicketPricingStrategy
    PricingStrategy <|.. FnbPricingStrategy
    PricingStrategy <|.. TimeBasedPricingStrategy
    TicketPricingStrategy ..> PricingLineType : TICKET
    FnbPricingStrategy ..> PricingLineType : FNB
    TimeBasedPricingStrategy ..> PricingLineType : TIME_BASED_SURCHARGE
    PricingEngine o-- PricingStrategy : strategiesByLine
    TicketPricingStrategy ..> PricingContext : reads
    FnbPricingStrategy ..> PricingContext : reads
    TimeBasedPricingStrategy ..> PricingContext : reads

    %% Decorator
    DiscountComponent <|.. NoDiscount
    DiscountComponent <|.. BaseDiscountDecorator
    BaseDiscountDecorator <|-- PromotionDiscountDecorator
    BaseDiscountDecorator <|-- MemberDiscountDecorator
    BaseDiscountDecorator o-- DiscountComponent : wraps
    DiscountComponent ..> DiscountResult : returns
    DiscountComponent ..> PricingContext : reads context
    PricingEngine ..> DiscountComponent : builds chain

    %% Orchestrator flow
    BookingServiceImpl --> PricingValidationHandler : 1. validate
    BookingServiceImpl --> PricingContextBuilder : injects & uses
    PricingContextBuilder ..> PricingValidationContext : reads showtime & promotion
    PricingContextBuilder ..> PricingContext : produces
    BookingServiceImpl --> IPricingEngine : 3. calculate
    CachingPricingEngineProxy --> PricingEngine : cache miss delegate
    PricingEngine ..> PricingContext : uses
    PricingEngine ..> PriceBreakdownDTO : returns
    BookingServiceImpl ..> PricingValidationContext : builds
    PricingContextBuilder ..> FnbItem : loads prices via repo
```



