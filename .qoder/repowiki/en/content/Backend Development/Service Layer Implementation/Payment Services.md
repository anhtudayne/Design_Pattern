# Payment Services

<cite>
**Referenced Files in This Document**
- [PaymentServiceImpl.java](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java)
- [MomoServiceImpl.java](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java)
- [PaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java)
- [PaymentStrategyFactory.java](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java)
- [CashPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java)
- [DemoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java)
- [MomoPaymentStrategy.java](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java)
- [PaymentMethod.java](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentMethod.java)
- [AbstractCheckoutTemplate.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java)
- [MomoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java)
- [DemoCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java)
- [StaffCashCheckoutProcess.java](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java)
- [MomoCallbackRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoCallbackRequest.java)
- [MomoPaymentRequest.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java)
- [MomoPaymentResponse.java](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Security and Compliance](#security-and-compliance)
9. [Troubleshooting Guide](#troubleshooting-guide)
10. [Conclusion](#conclusion)

## Introduction
This document explains the payment processing services for the cinema booking system. It covers:
- Payment service implementation for retrieving payment history and details
- MoMo payment integration, including payment creation and callback handling
- Payment strategy pattern supporting multiple payment methods (Cash, Demo, Momo)
- End-to-end payment workflows, status tracking, error handling, and refund considerations
- Security and PCI compliance guidance

## Project Structure
The payment subsystem is organized around:
- Strategy pattern for payment methods
- Template method for checkout flows
- Service implementations for payment history and MoMo integration
- DTOs for MoMo requests and callbacks

```mermaid
graph TB
subgraph "Payment Strategy Layer"
PS["PaymentStrategy"]
PSF["PaymentStrategyFactory"]
CASH["CashPaymentStrategy"]
DEMO["DemoPaymentStrategy"]
MOMO["MomoPaymentStrategy"]
end
subgraph "Checkout Template Layer"
ACT["AbstractCheckoutTemplate"]
MCP["MomoCheckoutProcess"]
DCP["DemoCheckoutProcess"]
SCP["StaffCashCheckoutProcess"]
end
subgraph "Service Layer"
PIMPL["PaymentServiceImpl"]
MIMPL["MomoServiceImpl"]
end
subgraph "DTOs"
MPR["MomoPaymentRequest"]
MPResp["MomoPaymentResponse"]
MCB["MomoCallbackRequest"]
end
PSF --> PS
PS --> MCP
PS --> DCP
PS --> SCP
ACT --> MCP
ACT --> DCP
ACT --> SCP
MCP --> MIMPL
MCP --> MPR
MCP --> MPResp
MIMPL --> MPResp
MIMPL --> MCB
PIMPL --> |"queries"| Payment["Payment entity"]
```

**Diagram sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [DemoPaymentStrategy.java:1-36](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java#L1-L36)
- [MomoPaymentStrategy.java:1-27](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java#L1-L27)
- [AbstractCheckoutTemplate.java:1-182](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L1-L182)
- [MomoCheckoutProcess.java:1-70](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java#L1-L70)
- [DemoCheckoutProcess.java:1-131](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java#L1-L131)
- [StaffCashCheckoutProcess.java:1-129](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java#L1-L129)
- [PaymentServiceImpl.java:1-69](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L1-L69)
- [MomoServiceImpl.java:1-95](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L1-L95)
- [MomoPaymentRequest.java:1-23](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java#L1-L23)
- [MomoPaymentResponse.java:1-18](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java#L1-L18)
- [MomoCallbackRequest.java:1-21](file://backend/src/main/java/com/cinema/booking/dtos/MomoCallbackRequest.java#L1-L21)

**Section sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [AbstractCheckoutTemplate.java:1-182](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L1-L182)
- [PaymentServiceImpl.java:1-69](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L1-L69)
- [MomoServiceImpl.java:1-95](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L1-L95)

## Core Components
- PaymentServiceImpl: Provides user payment history retrieval and payment detail lookup. It maps Payment entities to PaymentHistoryDTO and enriches it with movie and showtime metadata via Ticket and Showtime relations.
- MomoServiceImpl: Handles MoMo payment creation by building signed requests, sending them to the MoMo endpoint, and capturing the response. It also exposes a placeholder for signature verification from callbacks.
- PaymentStrategy and PaymentStrategyFactory: Define and register payment strategies per method (MOMO, DEMO, CASH). Factory ensures all methods are registered and supports type-safe retrieval.
- Template methods for checkout: AbstractCheckoutTemplate orchestrates shared steps (user validation, seat validation, pricing, promotion reservation, F&B reservation, payment processing, booking finalization). Subclasses implement payment-specific logic.

**Section sources**
- [PaymentServiceImpl.java:1-69](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L1-L69)
- [MomoServiceImpl.java:1-95](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L1-L95)
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [AbstractCheckoutTemplate.java:1-182](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L1-L182)

## Architecture Overview
The payment architecture separates concerns across layers:
- Strategy layer selects the appropriate payment method
- Template method layer defines the checkout flow and delegates payment-specific steps
- Service layer handles persistence and external integrations (e.g., MoMo)
- DTO layer models request/response contracts for MoMo

```mermaid
sequenceDiagram
participant Client as "Client"
participant Strategy as "PaymentStrategy"
participant Template as "AbstractCheckoutTemplate"
participant Impl as "MomoCheckoutProcess"
participant MoMo as "MomoServiceImpl"
participant Repo as "PaymentRepository"
Client->>Strategy : "checkout(request)"
Strategy->>Template : "checkout(request)"
Template->>Template : "validateUser()"
Template->>Template : "validateSeats()"
Template->>Template : "calculatePrice()"
Template->>Template : "findPromotion()"
Template->>Template : "createBooking()"
Template->>Template : "reserveFnb()"
Template->>Impl : "processPayment(...)"
Impl->>MoMo : "createPayment(orderId, amount, orderInfo, extraData)"
MoMo-->>Impl : "MomoPaymentResponse(payUrl)"
Impl->>Repo : "save PENDING Payment"
Template->>Impl : "finalizeBooking(...)"
Impl-->>Client : "CheckoutResult(paymentResult=payUrl)"
```

**Diagram sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [AbstractCheckoutTemplate.java:53-95](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L53-L95)
- [MomoCheckoutProcess.java:40-68](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java#L40-L68)
- [MomoServiceImpl.java:42-86](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L42-L86)
- [PaymentStrategyFactory.java:33-39](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L33-L39)

## Detailed Component Analysis

### PaymentServiceImpl
Responsibilities:
- Retrieve user payment history and map to DTOs
- Fetch payment details by ID with error handling
- Enrich DTOs with movie/showtime metadata via Tickets and Showtimes

Processing logic:
- Queries payments for a user and transforms each Payment to PaymentHistoryDTO
- Builds nested summary objects for booking, showtime, and movie
- Defaults to PENDING status if entity status is null

```mermaid
flowchart TD
Start(["getUserPaymentHistory(userId)"]) --> Query["Query PaymentRepository by userId"]
Query --> Map["Map each Payment to PaymentHistoryDTO"]
Map --> Enrich["Load Tickets/Showtime/Movie for metadata"]
Enrich --> Build["Build nested Booking/Showtime/Movie summaries"]
Build --> Return["Return List<PaymentHistoryDTO>"]
DetailStart(["getPaymentDetails(paymentId)"]) --> Find["Find Payment by ID"]
Find --> Exists{"Found?"}
Exists --> |Yes| ReturnDetail["Return Payment"]
Exists --> |No| Throw["Throw 'not found' runtime exception"]
```

**Diagram sources**
- [PaymentServiceImpl.java:24-67](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L24-L67)

**Section sources**
- [PaymentServiceImpl.java:1-69](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L1-L69)

### MomoServiceImpl
Responsibilities:
- Build MoMo payment requests with HMAC-SHA256 signature
- Send requests to MoMo endpoint and capture response
- Provide a placeholder for verifying MoMo callback signatures

Implementation highlights:
- Constructs a raw signature string from ordered parameters and signs with secret key
- Sends HTTP POST to create payment endpoint
- Logs warnings if payUrl is missing in response
- Exposes verifySignature for future callback verification

```mermaid
sequenceDiagram
participant Client as "MomoCheckoutProcess"
participant MoMo as "MomoServiceImpl"
participant Sec as "SecurityUtils.hmacSha256"
participant Endpoint as "MoMo /create"
Client->>MoMo : "createPayment(orderId, amount, orderInfo, extraData)"
MoMo->>MoMo : "Build rawHash from params"
MoMo->>Sec : "Sign rawHash with secretKey"
Sec-->>MoMo : "signature"
MoMo->>Endpoint : "POST /create with signed request"
Endpoint-->>MoMo : "MomoPaymentResponse"
MoMo-->>Client : "payUrl or null"
```

**Diagram sources**
- [MomoServiceImpl.java:42-86](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L42-L86)
- [MomoPaymentRequest.java:1-23](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java#L1-L23)
- [MomoPaymentResponse.java:1-18](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java#L1-L18)

**Section sources**
- [MomoServiceImpl.java:1-95](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L1-L95)
- [MomoPaymentRequest.java:1-23](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentRequest.java#L1-L23)
- [MomoPaymentResponse.java:1-18](file://backend/src/main/java/com/cinema/booking/dtos/MomoPaymentResponse.java#L1-L18)

### Payment Strategy Pattern
The strategy pattern enables pluggable payment methods:
- PaymentStrategy defines the contract for checkout and method identification
- PaymentStrategyFactory registers strategies and validates completeness
- Concrete strategies delegate to template methods for checkout logic

```mermaid
classDiagram
class PaymentStrategy {
+getPaymentMethod() PaymentMethod
+checkout(request) CheckoutResult
}
class PaymentStrategyFactory {
+getStrategy(method) PaymentStrategy
+getStrategy(method, type) T
}
class CashPaymentStrategy {
+getPaymentMethod() PaymentMethod
+checkout(request) CheckoutResult
+buildResult(booking, payment, price) Map
}
class DemoPaymentStrategy {
+getPaymentMethod() PaymentMethod
+checkout(request) CheckoutResult
+buildDemoResult(booking, payment, price) Map
}
class MomoPaymentStrategy {
+getPaymentMethod() PaymentMethod
+checkout(request) CheckoutResult
}
class PaymentMethod {
<<enum>>
+MOMO
+DEMO
+CASH
}
PaymentStrategy <|.. CashPaymentStrategy
PaymentStrategy <|.. DemoPaymentStrategy
PaymentStrategy <|.. MomoPaymentStrategy
PaymentStrategyFactory --> PaymentStrategy : "manages"
PaymentStrategy --> PaymentMethod : "returns"
```

**Diagram sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [DemoPaymentStrategy.java:1-36](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java#L1-L36)
- [MomoPaymentStrategy.java:1-27](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java#L1-L27)
- [PaymentMethod.java:1-22](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentMethod.java#L1-L22)

**Section sources**
- [PaymentStrategy.java:1-15](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java#L1-L15)
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [CashPaymentStrategy.java:1-40](file://backend/src/main/java/com/cinema/booking/services/payment/CashPaymentStrategy.java#L1-L40)
- [DemoPaymentStrategy.java:1-36](file://backend/src/main/java/com/cinema/booking/services/payment/DemoPaymentStrategy.java#L1-L36)
- [MomoPaymentStrategy.java:1-27](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java#L1-L27)
- [PaymentMethod.java:1-22](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentMethod.java#L1-L22)

### Checkout Template Methods
AbstractCheckoutTemplate defines the shared checkout flow:
- Validates user and seats
- Calculates price and reserves promotions/F&B
- Creates booking and delegates payment processing and finalization to subclasses

```mermaid
flowchart TD
A["checkout(request)"] --> B["validateUser(userId)"]
B --> C["validateSeats(showtimeId, seatIds)"]
C --> D["calculatePrice(request)"]
D --> E["findPromotion(promoCode)"]
E --> F["createBooking(customer, promotion, status)"]
F --> G["reserveFnbItems(request.fnbs)"]
G --> H["processPayment(booking, price, request)"]
H --> I["finalizeBooking(booking, price, request, paymentResult)"]
I --> J["rollbackReservedResources() if cancelled"]
J --> K["return CheckoutResult"]
```

**Diagram sources**
- [AbstractCheckoutTemplate.java:53-95](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L53-L95)

Subclasses implement payment-specific behavior:
- MomoCheckoutProcess: sets initial status to PENDING, creates MoMo payment, persists PENDING Payment
- DemoCheckoutProcess: creates Payment immediately (SUCCESS/FAILED based on demo flag), issues tickets, updates customer spending, sends email
- StaffCashCheckoutProcess: sets initial status to CONFIRMED, records SUCCESS Payment, issues tickets, updates customer spending

**Section sources**
- [AbstractCheckoutTemplate.java:1-182](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L1-L182)
- [MomoCheckoutProcess.java:1-70](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java#L1-L70)
- [DemoCheckoutProcess.java:1-131](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java#L1-L131)
- [StaffCashCheckoutProcess.java:1-129](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/StaffCashCheckoutProcess.java#L1-L129)

### Payment Initiation, Status Updates, and Refunds
- Initiation: Strategies delegate to template methods; MoMo strategy builds extraData containing booking and seat info, requests payUrl from MoMo, and persists a PENDING Payment
- Status updates: The current implementation logs MoMo responses and stores PENDING Payment. Callback verification and status updates are indicated by placeholders in MoMo service and callback DTOs
- Refunds: Not implemented in the current codebase; recommended approach is to introduce a Refund entity and update Payment status accordingly

```mermaid
sequenceDiagram
participant Client as "Client"
participant Strategy as "MomoPaymentStrategy"
participant Template as "MomoCheckoutProcess"
participant MoMo as "MomoServiceImpl"
participant DB as "PaymentRepository"
Client->>Strategy : "checkout(request)"
Strategy->>Template : "checkout(request)"
Template->>MoMo : "createPayment(...)"
MoMo-->>Template : "MomoPaymentResponse(payUrl)"
Template->>DB : "save Payment(status=PENDING)"
Template-->>Client : "CheckoutResult(paymentResult=payUrl)"
```

**Diagram sources**
- [MomoPaymentStrategy.java:22-25](file://backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java#L22-L25)
- [MomoCheckoutProcess.java:46-68](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java#L46-L68)
- [MomoServiceImpl.java:42-86](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L42-L86)

## Dependency Analysis
- PaymentStrategyFactory depends on all PaymentStrategy beans to populate an internal map and validate completeness
- Template methods depend on repositories and services for persistence and business logic
- MomoCheckoutProcess depends on MomoService for payment creation
- PaymentServiceImpl depends on PaymentRepository and TicketRepository for history enrichment

```mermaid
graph LR
PSF["PaymentStrategyFactory"] --> PS["PaymentStrategy beans"]
PS --> STRATS["Cash/Demo/Momo Strategies"]
STRATS --> TPL["AbstractCheckoutTemplate subclasses"]
TPL --> REPO["Repositories"]
TPL --> SVC["Services"]
MCP["MomoCheckoutProcess"] --> MIMPL["MomoServiceImpl"]
PIMPL["PaymentServiceImpl"] --> PREPO["PaymentRepository"]
PIMPL --> TREPO["TicketRepository"]
```

**Diagram sources**
- [PaymentStrategyFactory.java:18-31](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L18-L31)
- [MomoCheckoutProcess.java:23-37](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java#L23-L37)
- [PaymentServiceImpl.java:17-21](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L17-L21)

**Section sources**
- [PaymentStrategyFactory.java:1-49](file://backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java#L1-L49)
- [MomoCheckoutProcess.java:1-70](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/MomoCheckoutProcess.java#L1-L70)
- [PaymentServiceImpl.java:1-69](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L1-L69)

## Performance Considerations
- Template method reduces duplication and centralizes validations, minimizing repeated work across strategies
- Payment history enrichment queries Tickets and Showtimes; ensure proper indexing on booking_id and showtime/movie joins
- MoMo request signing and HTTP calls should be optimized with connection pooling and timeouts
- Retry/backoff strategies for customer spending updates mitigate transient deadlocks

## Security and Compliance
- PCI DSS: Do not log or persist cardholder data; rely on MoMo for sensitive data handling
- Signature verification: Implement MoMo callback signature verification using the documented raw hash format and secret key
- Input validation: Validate orderId, amount, and extraData before invoking MoMo; sanitize and limit payload sizes
- Secure storage: Store MoMo credentials via environment variables or secure vaults; avoid committing secrets
- HTTPS and TLS: Ensure all integrations use TLS 1.2+ and up-to-date certificates
- Audit logging: Log only non-sensitive fields (e.g., resultCode, responseTime) for diagnostics

## Troubleshooting Guide
Common issues and resolutions:
- Payment not found: PaymentServiceImpl throws a runtime exception when paymentId does not exist; verify the ID and associated booking
- Missing payUrl: MomoServiceImpl logs a warning when payUrl is null; check MoMo endpoint response and required parameters
- Signature mismatch: Implement verifySignature using the documented raw hash construction and compare with MoMo’s signature field
- Seat conflicts: AbstractCheckoutTemplate.validateSeats prevents purchases of already-sold seats; prompt users to select alternate seats
- Demo failures: DemoCheckoutProcess sets Payment status to FAILED and cancels booking; confirm demoSuccess flag correctness

**Section sources**
- [PaymentServiceImpl.java:30-34](file://backend/src/main/java/com/cinema/booking/services/impl/PaymentServiceImpl.java#L30-L34)
- [MomoServiceImpl.java:78-84](file://backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java#L78-L84)
- [AbstractCheckoutTemplate.java:133-139](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/AbstractCheckoutTemplate.java#L133-L139)
- [DemoCheckoutProcess.java:56-62](file://backend/src/main/java/com/cinema/booking/services/template_method/checkout/DemoCheckoutProcess.java#L56-L62)

## Conclusion
The payment subsystem leverages the Strategy and Template Method patterns to support multiple payment methods while centralizing shared checkout logic. PaymentServiceImpl provides robust payment history and detail retrieval, while MomoServiceImpl integrates with MoMo for payment initiation. The architecture is extensible for additional payment methods and ready for callback verification and refund processing enhancements.