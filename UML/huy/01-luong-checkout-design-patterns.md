# Luồng checkout / thanh toán — Design patterns

**Áp dụng:** **Template Method** (khung `checkout`) + **Strategy** (mỗi kênh thanh toán) + **Factory Method** (chọn strategy theo `PaymentMethod`).

---

## 1. Class diagram — các lớp pattern (backend)

```mermaid
classDiagram
direction TB

class PaymentMethod {
  <<enumeration>>
  MOMO
  DEMO
}

class PaymentStrategy {
  <<interface>>
  +getPaymentMethod() PaymentMethod
  +checkout(CheckoutRequest) CheckoutResult
}

class MomoPaymentStrategy {
  -momoCheckoutProcess: MomoCheckoutProcess
  +checkout(CheckoutRequest) CheckoutResult
}

class DemoPaymentStrategy {
  -demoCheckoutProcess: DemoCheckoutProcess
  +checkout(CheckoutRequest) CheckoutResult
  +buildDemoResult(...) Map
}

class PaymentStrategyFactory {
  <<component>>
  -strategies: EnumMap
  +getStrategy(PaymentMethod) PaymentStrategy
  +getStrategy(PaymentMethod, Class subtype) PaymentStrategy
}

PaymentStrategy <|.. MomoPaymentStrategy
PaymentStrategy <|.. DemoPaymentStrategy
PaymentStrategyFactory ..> PaymentStrategy : creates / resolves

MomoPaymentStrategy --> MomoCheckoutProcess
DemoPaymentStrategy --> DemoCheckoutProcess

class AbstractCheckoutTemplate {
  <<abstract>>
  +checkout(CheckoutRequest) CheckoutResult
  #processPayment(...) Object
  #finalizeBooking(...) void
}

class MomoCheckoutProcess {
  +processPayment(...)
  +finalizeBooking(...)
}

class DemoCheckoutProcess {
  +processPayment(...)
  +finalizeBooking(...)
}

AbstractCheckoutTemplate <|-- MomoCheckoutProcess
AbstractCheckoutTemplate <|-- DemoCheckoutProcess

note for AbstractCheckoutTemplate "Template Method:\nfinal checkout() định nghĩa các bước;\nhook ở subclass."
note for PaymentStrategyFactory "Factory:\nmap enum → bean strategy\n(lúc khởi tạo Spring)."
```

---

## 2. Sequence diagram — checkout MoMo (tạo booking + payUrl)

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant PaymentController
    participant CheckoutServiceImpl
    participant PaymentStrategyFactory
    participant MomoPaymentStrategy
    participant MomoCheckoutProcess as AbstractTemplate_Momo

    Client->>PaymentController: POST /checkout (body + paymentMethod?)
    PaymentController->>CheckoutServiceImpl: createBooking(..., paymentMethod)
    CheckoutServiceImpl->>CheckoutServiceImpl: PaymentMethod.fromString (MOMO)
    CheckoutServiceImpl->>PaymentStrategyFactory: getStrategy(MOMO)
    PaymentStrategyFactory-->>CheckoutServiceImpl: MomoPaymentStrategy
    CheckoutServiceImpl->>MomoPaymentStrategy: checkout(CheckoutRequest)
    MomoPaymentStrategy->>MomoCheckoutProcess: checkout(request)
    Note over MomoCheckoutProcess: Template Method:\nvalidate → price →\ncreate booking → F&B →\nprocessPayment (MoMo API) →\nfinalize (Payment PENDING)
    MomoCheckoutProcess-->>MomoPaymentStrategy: CheckoutResult
    MomoPaymentStrategy-->>CheckoutServiceImpl: CheckoutResult
    CheckoutServiceImpl-->>PaymentController: payUrl (String)
    PaymentController-->>Client: { payUrl }
```

---

## 3. Sequence diagram — demo checkout (không gọi cổng thật)

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant PaymentController
    participant CheckoutServiceImpl
    participant PaymentStrategyFactory
    participant DemoPaymentStrategy
    participant DemoCheckoutProcess as AbstractTemplate_Demo

    Client->>PaymentController: POST /checkout/demo?success=...
    PaymentController->>CheckoutServiceImpl: processDemoCheckout(...)
    CheckoutServiceImpl->>PaymentStrategyFactory: getStrategy(DEMO, DemoPaymentStrategy.class)
    PaymentStrategyFactory-->>CheckoutServiceImpl: DemoPaymentStrategy
    CheckoutServiceImpl->>DemoPaymentStrategy: checkout(CheckoutRequest)
    DemoPaymentStrategy->>DemoCheckoutProcess: checkout(request)
    Note over DemoCheckoutProcess: Cùng skeleton template;\nkhác hook: payment/ticket ngay
    DemoCheckoutProcess-->>DemoPaymentStrategy: CheckoutResult
    DemoPaymentStrategy-->>CheckoutServiceImpl: CheckoutResult
    CheckoutServiceImpl->>DemoPaymentStrategy: buildDemoResult(booking, payment, price)
    DemoPaymentStrategy-->>CheckoutServiceImpl: Map JSON
    CheckoutServiceImpl-->>PaymentController: Map
    PaymentController-->>Client: 200 + body demo
```
