# Luồng checkout / thanh toán — Strategy và Factory

**Áp dụng:** **Strategy** (mỗi kênh thanh toán một lớp triển khai `PaymentStrategy`) + **Factory** (`PaymentStrategyFactory` chọn đúng strategy theo `PaymentMethod`).

---

## 1. Class diagram — Strategy + Factory

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
  +getPaymentMethod() PaymentMethod
  +checkout(CheckoutRequest) CheckoutResult
}

class DemoPaymentStrategy {
  +getPaymentMethod() PaymentMethod
  +checkout(CheckoutRequest) CheckoutResult
  +buildDemoResult(...) Map
}

class PaymentStrategyFactory {
  <<component>>
  -strategies: EnumMap
  +getStrategy(PaymentMethod) PaymentStrategy
  +getStrategy(PaymentMethod, Class subtype) PaymentStrategy
}

class CheckoutServiceImpl {
  -paymentStrategyFactory: PaymentStrategyFactory
  +createBooking(...) String
  +processDemoCheckout(...) Map
}

PaymentStrategy <|.. MomoPaymentStrategy
PaymentStrategy <|.. DemoPaymentStrategy
PaymentStrategyFactory ..> PaymentStrategy : resolves
CheckoutServiceImpl --> PaymentStrategyFactory

note for PaymentStrategy "Strategy:\nđóng gói thuật toán checkout\ntheo từng kênh."
note for PaymentStrategyFactory "Factory:\nmap PaymentMethod → bean strategy\n(khởi tạo Spring)."
```

---

## 2. Sequence diagram — checkout MoMo (factory chọn strategy, strategy thực hiện checkout)

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant PaymentController
    participant CheckoutServiceImpl
    participant PaymentStrategyFactory
    participant MomoPaymentStrategy

    Client->>PaymentController: POST /checkout (body + paymentMethod)
    PaymentController->>CheckoutServiceImpl: createBooking(..., paymentMethod)
    CheckoutServiceImpl->>CheckoutServiceImpl: PaymentMethod.fromString → MOMO
    CheckoutServiceImpl->>PaymentStrategyFactory: getStrategy(MOMO)
    PaymentStrategyFactory-->>CheckoutServiceImpl: MomoPaymentStrategy
    CheckoutServiceImpl->>MomoPaymentStrategy: checkout(CheckoutRequest)
    Note over MomoPaymentStrategy: Strategy thực hiện toàn bộ\nluồng checkout kênh MoMo
    MomoPaymentStrategy-->>CheckoutServiceImpl: CheckoutResult (payUrl)
    CheckoutServiceImpl-->>PaymentController: payUrl (String)
    PaymentController-->>Client: { payUrl }
```

---

## 3. Sequence diagram — demo checkout (factory + subtype, strategy demo)

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant PaymentController
    participant CheckoutServiceImpl
    participant PaymentStrategyFactory
    participant DemoPaymentStrategy

    Client->>PaymentController: POST /checkout/demo?success=...
    PaymentController->>CheckoutServiceImpl: processDemoCheckout(...)
    CheckoutServiceImpl->>PaymentStrategyFactory: getStrategy(DEMO, DemoPaymentStrategy.class)
    PaymentStrategyFactory-->>CheckoutServiceImpl: DemoPaymentStrategy
    CheckoutServiceImpl->>DemoPaymentStrategy: checkout(CheckoutRequest)
    Note over DemoPaymentStrategy: Strategy demo:\nkhông gọi cổng thật
    DemoPaymentStrategy-->>CheckoutServiceImpl: CheckoutResult
    CheckoutServiceImpl->>DemoPaymentStrategy: buildDemoResult(booking, payment, price)
    DemoPaymentStrategy-->>CheckoutServiceImpl: Map JSON
    CheckoutServiceImpl-->>PaymentController: Map
    PaymentController-->>Client: 200 + body demo
```
