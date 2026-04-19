# UML — Composite (Pattern Only)

> Tài liệu chi tiết: [`docs/patterns/05-composite.md`](../../docs/patterns/05-composite.md)

**Ghi chú kỹ thuật:** `DashboardStatsComposite` nhận `List<StatsComponent>` qua constructor (Spring DI). Các leaf là bean `@Component`; composite lọc bỏ chính nó để tránh đệ quy. **Không** có phương thức `add()` — cấu trúc cây do container dựng.

```mermaid
classDiagram
  direction TB

  class StatsComponent {
    <<interface>>
    +collect(target: Map~String,Object~)
  }

  class DashboardStatsComposite {
    <<component>>
    -children: List~StatsComponent~
    +DashboardStatsComposite(allComponents: List~StatsComponent~)
    +collect(target: Map~String,Object~)
  }

  class MovieStatsLeaf {
    +collect(target)
  }

  class UserStatsLeaf {
    +collect(target)
  }

  class ShowtimeStatsLeaf {
    +collect(target)
  }

  class FnbStatsLeaf {
    +collect(target)
  }

  class PromotionStatsLeaf {
    +collect(target)
  }

  class RevenueStatsLeaf {
    +collect(target)
  }

  class TicketStatsLeaf {
    +collect(target)
  }

  class DashboardController {
    <<controller>>
    -dashboardStatsComposite: DashboardStatsComposite
    +getStats() Map~String,Object~
  }

  %% Pattern structure
  StatsComponent <|.. DashboardStatsComposite
  StatsComponent <|.. MovieStatsLeaf
  StatsComponent <|.. UserStatsLeaf
  StatsComponent <|.. ShowtimeStatsLeaf
  StatsComponent <|.. FnbStatsLeaf
  StatsComponent <|.. PromotionStatsLeaf
  StatsComponent <|.. RevenueStatsLeaf
  StatsComponent <|.. TicketStatsLeaf
  DashboardStatsComposite "1" o-- "*" StatsComponent : children (DI)
  DashboardController --> DashboardStatsComposite : collect()
```
