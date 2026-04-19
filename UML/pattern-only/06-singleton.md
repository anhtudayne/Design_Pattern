# UML — Singleton via Spring IoC (Pattern Only)

> Tài liệu chi tiết: [`docs/patterns/06-singleton.md`](../../docs/patterns/06-singleton.md)

```mermaid
classDiagram
  direction TB

  class RestTemplateConfig {
    <<configuration>>
    +restTemplate() RestTemplate
  }

  class RestTemplate {
    <<singleton-bean>>
    +getForObject(url, responseType) T
    +postForObject(url, request, responseType) T
    +exchange(url, method, entity, responseType) ResponseEntity~T~
  }

  class SpringApplicationContext {
    <<container>>
    +getBean(type: Class~T~) T
  }

  class MomoServiceImpl {
    <<service>>
    -restTemplate: RestTemplate
    +createPayment(orderId, amountVnd, orderInfo, extraData) MomoPaymentResponse
    +verifySignature(callback) boolean
  }

  %% Pattern structure — Singleton via Spring IoC
  RestTemplateConfig ..> RestTemplate : "@Bean creates"
  SpringApplicationContext --> RestTemplate : "manages as singleton"
  MomoServiceImpl --> RestTemplate : "@Autowired via constructor"

  note for RestTemplate "Singleton scope (default Spring):\nSpring IoC đảm bảo chỉ tạo 1 instance\nduy nhất, inject cùng bean cho tất cả client."
```

> **Lưu ý:** `SecurityConfig` không sử dụng `RestTemplate` — class này chỉ cấu hình phân quyền HTTP/JWT, không liên quan đến pattern Singleton này.
