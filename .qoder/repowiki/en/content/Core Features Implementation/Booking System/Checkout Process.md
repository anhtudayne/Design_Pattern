# Checkout Process

<cite>
**Referenced Files in This Document**
- [AbstractCheckoutValidationHandler.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/AbstractCheckoutValidationHandler.java)
- [CheckoutValidationConfig.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java)
- [CheckoutValidationContext.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationContext.java)
- [CheckoutValidationHandler.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationHandler.java)
- [MaxSeatsHandler.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/MaxSeatsHandler.java)
- [SeatsNotSoldHandler.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/SeatsNotSoldHandler.java)
- [ShowtimeExistsHandler.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/ShowtimeExistsHandler.java)
- [UserExistsHandler.java](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/UserExistsHandler.java)
- [AbstractCheckoutTemplate.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java)
- [DemoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java)
- [MomoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java)
- [StaffCashCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java)
- [PaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java)
- [PaymentStrategyFactory.java](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java)
- [PaymentMethod.java](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentMethod.java)
- [CashPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java)
- [MomoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java)
- [DemoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)
- [TicketEmailNotifier.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketEmailNotifier.java)
- [TicketIssuer.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketIssuer.java)
- [BookingStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/BookingStatusUpdater.java)
- [PromotionInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PromotionInventoryRollback.java)
- [FnbInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/FnbInventoryRollback.java)
- [PaymentColleague.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentColleague.java)
- [MomoCallbackContext.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/MomoCallbackContext.java)
- [PaymentStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentStatusUpdater.java)
- [CheckoutServiceImpl.java](file://backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java)
- [CheckoutService.java](file://backend/src/main/java/com/cinema/booking/services/CheckoutService.java)
- [BookingController.java](file://backend/src/main/java/com/cinema/booking/controllers/BookingController.java)
- [PaymentController.java](file://backend/src/main/java/com/cinema/booking/controllers/PaymentController.java)
- [MomoCallbackRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoCallbackRequest.java)
- [MomoPaymentRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java)
- [MomoPaymentResponse.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java)
- [CheckoutRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutRequest.java)
- [CheckoutResult.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutResult.java)
- [PriceBreakdownDTO.java](file://backend/src/main/java/com/cinema/booking/dtos/PriceBreakdownDTO.java)
- [BookingDTO.java](file://backend/src/main/java/com/cinema/booking/dtos/BookingDTO.java)
- [Booking.java](file://backend/src/main/java/com/cinema/booking/entities/Booking.java)
- [Payment.java](file://backend/src/main/java/com/cinema/booking/entities/Payment.java)
- [Showtime.java](file://backend/src/main/java/com/cinema/booking/entities/Showtime.java)
- [User.java](file://backend/src/main/java/com/cinema/booking/entities/User.java)
- [TicketRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/TicketRepository.java)
- [ShowtimeRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/ShowtimeRepository.java)
- [UserRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/UserRepository.java)
- [CustomerRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/CustomerRepository.java)
- [BookingRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/BookingRepository.java)
- [PaymentRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/PaymentRepository.java)
- [TicketRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/TicketRepository.java)
- [SecurityConfig.java](file://backend/src/main/java/com/cinema/booking/config/SecurityConfig.java)
- [JwtUtils.java](file://backend/src/main/java/com/cinema/booking/security/JwtUtils.java)
- [JwtAuthFilter.java](file://backend/src/main/java/com/cinema/booking/security/JwtAuthFilter.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)
10. [Appendices](#appendices)

## Introduction
This document explains the checkout process implementation in the cinema booking system. It covers:
- The checkout validation pipeline using the Chain of Responsibility pattern
- The template method pattern for different checkout processes (MoMo, Demo, Staff Cash)
- Payment processing integration, status tracking, and callback handling
- Post-payment coordination using the Mediator pattern for booking updates, notifications, and inventory rollbacks
- Practical examples of checkout API endpoints, validation error handling, and payment failure recovery
- Security considerations for payment processing and PCI compliance

## Project Structure
The checkout system spans several packages:
- Validation pipeline: patterns/chainofresponsibility
- Template method checkout processes: services/template_method/checkout
- Payment strategies: services/payment
- Post-payment coordination: patterns/mediator
- Controllers and DTOs: controllers, dtos
- Entities and repositories: entities, repositories
- Security: config, security

```mermaid
graph TB
subgraph "Controllers"
BC["BookingController"]
PC["PaymentController"]
end
subgraph "Services"
CS["CheckoutService"]
PS["PaymentService"]
end
subgraph "Validation Pipeline"
CV["CheckoutValidationConfig"]
H1["MaxSeatsHandler"]
H2["UserExistsHandler"]
H3["ShowtimeExistsHandler"]
H4["SeatsNotSoldHandler"]
end
subgraph "Template Methods"
AT["AbstractCheckoutTemplate"]
MCP["MomoCheckoutProcess"]
DCP["DemoCheckoutProcess"]
SCP["StaffCashCheckoutProcess"]
end
subgraph "Payment Strategies"
PSF["PaymentStrategyFactory"]
PStr["PaymentStrategy"]
CPS["CashPaymentStrategy"]
MPS["MomoPaymentStrategy"]
DPS["DemoPaymentStrategy"]
end
subgraph "Post-Payment"
PM["PostPaymentMediator"]
TUP["TicketIssuer"]
NTF["TicketEmailNotifier"]
BSU["BookingStatusUpdater"]
PUR["PromotionInventoryRollback"]
FIR["FnbInventoryRollback"]
PSU["PaymentStatusUpdater"]
end
subgraph "Entities & Repositories"
B["Booking"]
P["Payment"]
S["Showtime"]
U["User"]
TR["TicketRepository"]
SR["ShowtimeRepository"]
UR["UserRepository"]
end
BC --> CS
PC --> PS
CS --> CV
CV --> H1 --> H2 --> H3 --> H4
CS --> PSF
PSF --> PStr
PStr --> CPS
PStr --> MPS
PStr --> DPS
PSF --> MCP
PSF --> DCP
PSF --> SCP
PS --> PM
PM --> TUP
PM --> NTF
PM --> BSU
PM --> PUR
PM --> FIR
PM --> PSU
H4 --> TR
H3 --> SR
H2 --> UR
CS --> B
PS --> P
```

**Diagram sources**
- [CheckoutValidationConfig.java:1-23](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java#L1-L23)
- [AbstractCheckoutTemplate.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java)
- [MomoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java)
- [DemoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java)
- [StaffCashCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [MomoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java)
- [DemoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)
- [TicketIssuer.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketIssuer.java)
- [TicketEmailNotifier.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketEmailNotifier.java)
- [BookingStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/BookingStatusUpdater.java)
- [PromotionInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PromotionInventoryRollback.java)
- [FnbInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/FnbInventoryRollback.java)
- [PaymentStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentStatusUpdater.java)
- [CheckoutServiceImpl.java](file://backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java)
- [BookingController.java](file://backend/src/main/java/com/cinema/booking/controllers/BookingController.java)
- [PaymentController.java](file://backend/src/main/java/com/cinema/booking/controllers/PaymentController.java)
- [TicketRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/TicketRepository.java)
- [ShowtimeRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/ShowtimeRepository.java)
- [UserRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/UserRepository.java)
- [Booking.java](file://backend/src/main/java/com/cinema/booking/entities/Booking.java)
- [Payment.java](file://backend/src/main/java/com/cinema/booking/entities/Payment.java)
- [Showtime.java](file://backend/src/main/java/com/cinema/booking/entities/Showtime.java)
- [User.java](file://backend/src/main/java/com/cinema/booking/entities/User.java)

**Section sources**
- [CheckoutValidationConfig.java:1-23](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java#L1-L23)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)

## Core Components
- Checkout validation pipeline: Validates seat selection, user existence, showtime existence, and seat availability using a chain of responsibility.
- Template method checkout processes: Encapsulates checkout steps per payment method (MoMo, Demo, Staff Cash).
- Payment strategies: Delegates checkout to appropriate template method process and encapsulates payment channel specifics.
- Post-payment mediator: Coordinates booking updates, ticket issuance, notifications, and inventory rollbacks after payment completion.

**Section sources**
- [AbstractCheckoutValidationHandler.java:1-21](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/AbstractCheckoutValidationHandler.java#L1-L21)
- [AbstractCheckoutTemplate.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java)
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)

## Architecture Overview
The checkout flow integrates validation, payment strategy selection, template method execution, and post-payment coordination.

```mermaid
sequenceDiagram
participant Client as "Client"
participant BC as "BookingController"
participant CS as "CheckoutService"
participant CV as "CheckoutValidationConfig"
participant PSF as "PaymentStrategyFactory"
participant PS as "PaymentStrategy"
participant TM as "Template Method (Checkout)"
participant PM as "PostPaymentMediator"
Client->>BC : "POST /api/bookings/checkout"
BC->>CS : "Initiate checkout"
CS->>CV : "Run validation chain"
CV-->>CS : "Validation OK or error"
CS->>PSF : "Select strategy by PaymentMethod"
PSF-->>CS : "PaymentStrategy"
CS->>PS : "checkout(request)"
PS->>TM : "Execute template method"
TM-->>PS : "CheckoutResult"
PS-->>CS : "CheckoutResult"
CS->>PM : "Notify post-payment actions"
PM-->>CS : "Coordination complete"
CS-->>BC : "Booking confirmed"
BC-->>Client : "201 Created / 400 Bad Request"
```

**Diagram sources**
- [BookingController.java](file://backend/src/main/java/com/cinema/booking/controllers/BookingController.java)
- [CheckoutServiceImpl.java](file://backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java)
- [CheckoutValidationConfig.java:1-23](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java#L1-L23)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [MomoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java)
- [DemoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)

## Detailed Component Analysis

### Checkout Validation Pipeline (Chain of Responsibility)
The validation pipeline ensures:
- Seat count adheres to maximum limits
- User exists (or defaults to walk-in guest)
- Showtime exists
- Selected seats are not yet sold

```mermaid
classDiagram
class CheckoutValidationHandler {
<<interface>>
+setNext(next)
+handle(context)
}
class AbstractCheckoutValidationHandler {
-next : CheckoutValidationHandler
+setNext(next)
+handle(context)
#doHandle(context)
}
class MaxSeatsHandler {
+doHandle(context)
}
class UserExistsHandler {
-userRepository
-customerRepository
+doHandle(context)
}
class ShowtimeExistsHandler {
-showtimeRepository
+doHandle(context)
}
class SeatsNotSoldHandler {
-ticketRepository
+doHandle(context)
}
class CheckoutValidationContext {
+userId : Integer
+showtimeId : Integer
+seatIds : Integer[]
+promoCode : String
+user : User
+showtime : Showtime
}
CheckoutValidationHandler <|.. AbstractCheckoutValidationHandler
AbstractCheckoutValidationHandler <|-- MaxSeatsHandler
AbstractCheckoutValidationHandler <|-- UserExistsHandler
AbstractCheckoutValidationHandler <|-- ShowtimeExistsHandler
AbstractCheckoutValidationHandler <|-- SeatsNotSoldHandler
CheckoutValidationContext --> User
CheckoutValidationContext --> Showtime
```

**Diagram sources**
- [CheckoutValidationHandler.java:1-7](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationHandler.java#L1-L7)
- [AbstractCheckoutValidationHandler.java:1-21](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/AbstractCheckoutValidationHandler.java#L1-L21)
- [MaxSeatsHandler.java:1-20](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/MaxSeatsHandler.java#L1-L20)
- [UserExistsHandler.java:1-43](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/UserExistsHandler.java#L1-L43)
- [ShowtimeExistsHandler.java:1-21](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/ShowtimeExistsHandler.java#L1-L21)
- [SeatsNotSoldHandler.java:1-24](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/SeatsNotSoldHandler.java#L1-L24)
- [CheckoutValidationContext.java:1-22](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationContext.java#L1-L22)

```mermaid
flowchart TD
Start(["Start Validation"]) --> Max["MaxSeatsHandler<br/>Validate seat count"]
Max --> User["UserExistsHandler<br/>Validate user or create walk-in guest"]
User --> Showtime["ShowtimeExistsHandler<br/>Load showtime"]
Showtime --> Seats["SeatsNotSoldHandler<br/>Check seat availability"]
Seats --> End(["Validation Complete"])
```

**Diagram sources**
- [MaxSeatsHandler.java:1-20](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/MaxSeatsHandler.java#L1-L20)
- [UserExistsHandler.java:1-43](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/UserExistsHandler.java#L1-L43)
- [ShowtimeExistsHandler.java:1-21](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/ShowtimeExistsHandler.java#L1-L21)
- [SeatsNotSoldHandler.java:1-24](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/SeatsNotSoldHandler.java#L1-L24)

Key implementation notes:
- Handlers throw runtime exceptions on validation failures, short-circuiting the chain.
- The chain is configured in a Spring configuration bean wiring handlers in order.
- Entities are cached in the validation context for downstream use.

**Section sources**
- [CheckoutValidationConfig.java:1-23](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java#L1-L23)
- [CheckoutValidationContext.java:1-22](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationContext.java#L1-L22)
- [MaxSeatsHandler.java:1-20](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/MaxSeatsHandler.java#L1-L20)
- [UserExistsHandler.java:1-43](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/UserExistsHandler.java#L1-L43)
- [ShowtimeExistsHandler.java:1-21](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/ShowtimeExistsHandler.java#L1-L21)
- [SeatsNotSoldHandler.java:1-24](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/SeatsNotSoldHandler.java#L1-L24)

### Template Method Pattern for Checkout Processes
Different checkout processes share a common skeleton but implement specific steps:
- AbstractCheckoutTemplate defines the skeleton
- MomoCheckoutProcess handles MoMo-specific steps
- DemoCheckoutProcess handles demo steps
- StaffCashCheckoutProcess handles cash at box office

```mermaid
classDiagram
class AbstractCheckoutTemplate {
<<abstract>>
+checkout(request) CheckoutResult
#doCheckout(request) void
}
class MomoCheckoutProcess {
+doCheckout(request)
}
class DemoCheckoutProcess {
+doCheckout(request)
}
class StaffCashCheckoutProcess {
+doCheckout(request)
}
AbstractCheckoutTemplate <|-- MomoCheckoutProcess
AbstractCheckoutTemplate <|-- DemoCheckoutProcess
AbstractCheckoutTemplate <|-- StaffCashCheckoutProcess
```

**Diagram sources**
- [AbstractCheckoutTemplate.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java)
- [MomoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java)
- [DemoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java)
- [StaffCashCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java)

Operational flow for a payment method:
```mermaid
sequenceDiagram
participant PS as "PaymentStrategy"
participant TM as "Template Method"
participant Repo as "Repositories"
participant Med as "PostPaymentMediator"
PS->>TM : "checkout(request)"
TM->>Repo : "Reserve/lock seats"
TM->>Repo : "Create booking/payment records"
TM->>Med : "notifyComplete()"
Med-->>TM : "update booking status, send tickets, rollback inventory"
TM-->>PS : "CheckoutResult"
```

**Diagram sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [MomoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java)
- [DemoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java)
- [StaffCashCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)

**Section sources**
- [AbstractCheckoutTemplate.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java)
- [MomoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java)
- [DemoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java)
- [StaffCashCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java)

### Payment Processing Integration
Payment strategies encapsulate channel-specific logic and delegate checkout to the appropriate template method.

```mermaid
classDiagram
class PaymentStrategy {
<<interface>>
+getPaymentMethod() PaymentMethod
+checkout(request) CheckoutResult
}
class PaymentStrategyFactory {
-strategies : Map~PaymentMethod, PaymentStrategy~
+getStrategy(method) PaymentStrategy
+getStrategy(method, type) PaymentStrategy
}
class PaymentMethod {
<<enum>>
+MOMO
+DEMO
+CASH
+fromString(raw) PaymentMethod
}
class CashPaymentStrategy {
-cashCheckoutProcess : StaffCashCheckoutProcess
+checkout(request) CheckoutResult
+buildResult(booking, payment, price) Map
}
class MomoPaymentStrategy
class DemoPaymentStrategy
PaymentStrategy <|.. CashPaymentStrategy
PaymentStrategy <|.. MomoPaymentStrategy
PaymentStrategy <|.. DemoPaymentStrategy
PaymentStrategyFactory --> PaymentStrategy : "provides"
PaymentStrategy --> PaymentMethod : "exposes"
```

**Diagram sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [PaymentMethod.java:1-22](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentMethod.java#L1-L22)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [MomoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java)
- [DemoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java)

Payment method resolution and delegation:
- PaymentStrategyFactory validates and exposes strategies per PaymentMethod.
- PaymentStrategy.getPaymentMethod determines the channel.
- Strategies call their respective template method checkout.

**Section sources**
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentMethod.java:1-22](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentMethod.java#L1-L22)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)

### Post-Payment Coordination (Mediator)
After payment completes, the mediator coordinates:
- Booking status update
- Ticket issuance and email notification
- Inventory rollbacks for promotions and food/beverages

```mermaid
classDiagram
class PostPaymentMediator {
+notifyComplete(context)
}
class TicketIssuer {
+issueTickets(booking)
}
class TicketEmailNotifier {
+sendTicketEmail(booking)
}
class BookingStatusUpdater {
+updateStatus(booking, status)
}
class PromotionInventoryRollback {
+rollback(promoId, bookingId)
}
class FnbInventoryRollback {
+rollback(fnbs)
}
class PaymentStatusUpdater {
+updateStatus(payment, status)
}
class MomoCallbackContext {
+paymentId : String
+status : String
}
PostPaymentMediator --> TicketIssuer : "coordinates"
PostPaymentMediator --> TicketEmailNotifier : "coordinates"
PostPaymentMediator --> BookingStatusUpdater : "coordinates"
PostPaymentMediator --> PromotionInventoryRollback : "coordinates"
PostPaymentMediator --> FnbInventoryRollback : "coordinates"
PostPaymentMediator --> PaymentStatusUpdater : "coordinates"
```

**Diagram sources**
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)
- [TicketIssuer.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketIssuer.java)
- [TicketEmailNotifier.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketEmailNotifier.java)
- [BookingStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/BookingStatusUpdater.java)
- [PromotionInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PromotionInventoryRollback.java)
- [FnbInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/FnbInventoryRollback.java)
- [PaymentStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentStatusUpdater.java)
- [MomoCallbackContext.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/MomoCallbackContext.java)

**Section sources**
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)
- [TicketIssuer.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketIssuer.java)
- [TicketEmailNotifier.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/TicketEmailNotifier.java)
- [BookingStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/BookingStatusUpdater.java)
- [PromotionInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PromotionInventoryRollback.java)
- [FnbInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/FnbInventoryRollback.java)
- [PaymentStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentStatusUpdater.java)
- [MomoCallbackContext.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/MomoCallbackContext.java)

### API Endpoints and Examples
- POST /api/bookings/checkout
  - Request body: CheckoutRequest
  - Response: CheckoutResult
  - Validation errors: thrown during chain execution
- POST /api/payments/momo/create
  - Request body: MomoPaymentRequest
  - Response: MomoPaymentResponse
- POST /api/payments/momo/callback
  - Request body: MomoCallbackRequest
  - Response: Payment status update

Example request/response references:
- [CheckoutRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutRequest.java)
- [CheckoutResult.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutResult.java)
- [MomoPaymentRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java)
- [MomoPaymentResponse.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java)
- [MomoCallbackRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoCallbackRequest.java)

**Section sources**
- [BookingController.java](file://backend/src/main/java/com/cinema/booking/controllers/BookingController.java)
- [PaymentController.java](file://backend/src/main/java/com/cinema/booking/controllers/PaymentController.java)
- [CheckoutRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutRequest.java)
- [CheckoutResult.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutResult.java)
- [MomoPaymentRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java)
- [MomoPaymentResponse.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java)
- [MomoCallbackRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoCallbackRequest.java)

### Payment Failure Recovery Mechanisms
- Validation failures halt early with descriptive messages.
- Payment status updater tracks payment outcomes and triggers rollbacks via mediator.
- Inventory rollbacks for promotions and FnB items revert stock on failure.
- Retry strategies can re-run template method checkout with corrected data.

References:
- [PaymentStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentStatusUpdater.java)
- [PromotionInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PromotionInventoryRollback.java)
- [FnbInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/FnbInventoryRollback.java)

**Section sources**
- [PaymentStatusUpdater.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PaymentStatusUpdater.java)
- [PromotionInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PromotionInventoryRollback.java)
- [FnbInventoryRollback.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/FnbInventoryRollback.java)

## Dependency Analysis
The system exhibits low coupling and high cohesion:
- Validation handlers depend on repositories and are wired by CheckoutValidationConfig.
- PaymentStrategyFactory enforces presence of all supported strategies.
- Template methods depend on repositories and entities.
- Mediator decouples collaborators for post-payment actions.

```mermaid
graph LR
CV["CheckoutValidationConfig"] --> H1["MaxSeatsHandler"]
CV --> H2["UserExistsHandler"]
CV --> H3["ShowtimeExistsHandler"]
CV --> H4["SeatsNotSoldHandler"]
PSF["PaymentStrategyFactory"] --> CPS["CashPaymentStrategy"]
PSF --> MPS["MomoPaymentStrategy"]
PSF --> DPS["DemoPaymentStrategy"]
CPS --> SCP["StaffCashCheckoutProcess"]
MPS --> MCP["MomoCheckoutProcess"]
DPS --> DCP["DemoCheckoutProcess"]
PM["PostPaymentMediator"] --> TUP["TicketIssuer"]
PM --> NTF["TicketEmailNotifier"]
PM --> BSU["BookingStatusUpdater"]
PM --> PUR["PromotionInventoryRollback"]
PM --> FIR["FnbInventoryRollback"]
PM --> PSU["PaymentStatusUpdater"]
```

**Diagram sources**
- [CheckoutValidationConfig.java:1-23](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java#L1-L23)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [MomoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java)
- [DemoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java)
- [PostPaymentMediator.java](file://backend/src/main/java/com/cinema/booking/patterns/mediator/PostPaymentMediator.java)

**Section sources**
- [CheckoutValidationConfig.java:1-23](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/CheckoutValidationConfig.java#L1-L23)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)

## Performance Considerations
- Validation chain short-circuits on first failure to reduce overhead.
- Template methods encapsulate IO-bound steps; keep repository calls minimal and batch where possible.
- Mediator coordinates actions asynchronously where feasible to avoid blocking the main thread.
- Use caching for frequently accessed showtimes and user profiles.

## Troubleshooting Guide
Common validation errors and recovery:
- Seat limit exceeded: adjust selection to within allowed number.
- User not found: ensure authenticated user or use walk-in guest flow.
- Showtime missing: verify showtime identifier.
- Seat already sold: select another seat.

Payment-related issues:
- Callback mismatch: verify signature and payment identifiers.
- Duplicate callback: deduplicate by payment identifier.
- Rollback on failure: confirm inventory rollbacks executed.

Security and PCI compliance:
- Never log or persist raw PAN, CVV, or full cardholder data.
- Use PCI SAQ A or SAQ SP depending on data handling; prefer hosted fields/tokenization.
- Enforce HTTPS/TLS, validate SSL certificates, and rotate keys regularly.
- Implement strong session management and CSRF protection.
- Restrict access to payment endpoints via role-based authorization and JWT filters.

References for security:
- [SecurityConfig.java](file://backend/src/main/java/com/cinema/booking/config/SecurityConfig.java)
- [JwtUtils.java](file://backend/src/main/java/com/cinema/booking/security/JwtUtils.java)
- [JwtAuthFilter.java](file://backend/src/main/java/com/cinema/booking/security/JwtAuthFilter.java)

**Section sources**
- [MaxSeatsHandler.java:1-20](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/MaxSeatsHandler.java#L1-L20)
- [UserExistsHandler.java:1-43](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/UserExistsHandler.java#L1-L43)
- [ShowtimeExistsHandler.java:1-21](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/ShowtimeExistsHandler.java#L1-L21)
- [SeatsNotSoldHandler.java:1-24](file://backend/src/main/java/com/cinema/booking/patterns/chainofresponsibility/SeatsNotSoldHandler.java#L1-L24)
- [MomoCallbackRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoCallbackRequest.java)
- [SecurityConfig.java](file://backend/src/main/java/com/cinema/booking/config/SecurityConfig.java)
- [JwtUtils.java](file://backend/src/main/java/com/cinema/booking/security/JwtUtils.java)
- [JwtAuthFilter.java](file://backend/src/main/java/com/cinema/booking/security/JwtAuthFilter.java)

## Conclusion
The checkout process leverages well-defined patterns:
- Chain of Responsibility for robust, modular validation
- Template Method for consistent, extensible checkout flows
- Strategy for channel-specific payment handling
- Mediator for coordinated post-payment actions

This design supports scalability, maintainability, and clear separation of concerns while enabling secure payment processing aligned with PCI guidelines.

## Appendices
- Entities involved: Booking, Payment, Showtime, User
- DTOs involved: CheckoutRequest, CheckoutResult, PriceBreakdownDTO, BookingDTO
- Repositories involved: BookingRepository, PaymentRepository, TicketRepository, ShowtimeRepository, UserRepository

**Section sources**
- [Booking.java](file://backend/src/main/java/com/cinema/booking/entities/Booking.java)
- [Payment.java](file://backend/src/main/java/com/cinema/booking/entities/Payment.java)
- [Showtime.java](file://backend/src/main/java/com/cinema/booking/entities/Showtime.java)
- [User.java](file://backend/src/main/java/com/cinema/booking/entities/User.java)
- [CheckoutRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutRequest.java)
- [CheckoutResult.java](file://backend/src/main/java/com/cinema/booking/dtos/CheckoutResult.java)
- [PriceBreakdownDTO.java](file://backend/src/main/java/com/cinema/booking/dtos/PriceBreakdownDTO.java)
- [BookingDTO.java](file://backend/src/main/java/com/cinema/booking/dtos/BookingDTO.java)
- [BookingRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/BookingRepository.java)
- [PaymentRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/PaymentRepository.java)
- [TicketRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/TicketRepository.java)
- [ShowtimeRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/ShowtimeRepository.java)
- [UserRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/UserRepository.java)
- [CustomerRepository.java](file://backend/src/main/java/com/cinema/booking/repositories/CustomerRepository.java)