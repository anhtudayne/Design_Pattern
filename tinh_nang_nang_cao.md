Tính năng: Dynamic Pricing Engine (Hệ thống tính giá thông minh)
Mô tả tính năng:
Giá vé tự động điều chỉnh dựa trên nhiều yếu tố: ngày lễ/cuối tuần, suất chiếu sớm (Early Bird), tỷ lệ lấp đầy phòng (>80% → phụ thu), hạng thành viên (VIP/VVIP giảm giá), combo F&B kèm vé. Thay vì hardcode giá, hệ thống tính giá linh hoạt theo luật nghiệp vụ.

Pattern kết hợp: 5 pattern

┌──────────────────────────────────────────────────────────┐
│              DYNAMIC PRICING ENGINE                       │
│                                                          │
│  ① Specification ─── Xác định bối cảnh giá              │
│       │   isWeekend() AND isHoliday() AND isVIP()        │
│       ▼                                                  │
│  ② Strategy (MỚI) ─── Chọn PricingStrategy:             │
│       │   WeekendPricing / HolidayPricing / EarlyBird   │
│       ▼                                                  │
│  ③ Decorator (MỚI) ─── Stack các modifier lên nhau:     │
│       │   BasePriceCalculator                             │
│       │   └─ SeatTypeDecorator (VIP +20k)                │
│       │      └─ WeekendDecorator (+15%)                  │
│       │         └─ MemberDiscountDecorator (-10% VIP)    │
│       │            └─ VoucherDecorator (-50k)            │
│       ▼                                                  │
│  ④ Proxy (Cache) ─── Cache kết quả tính giá 5 phút      │
│       │   (tránh tính lại với mỗi request)               │
│       ▼                                                  │
│  ⑤ Chain of Responsibility ─── Validate giá cuối:        │
│           MinPriceHandler (không < 0)                     │
│           MaxDiscountHandler (giảm max 50%)               │
│           FraudDetectionHandler (giá bất thường?)         │
└──────────────────────────────────────────────────────────┘