# Tai lieu de xuat Design Pattern cho Role User

Tai lieu nay tong hop cac de xuat da duoc thu hep chi cho cac luong nghiep vu cua `USER` (khach hang dat ve): chon ghe, tinh gia, thanh toan, va hoan tat don.

## Table of Contents


| Uu tien              | Chuc nang (Role User)                      | Pattern de xuat                               |
| -------------------- | ------------------------------------------ | --------------------------------------------- |
| 1 - Cuc ky uu tien   | Checkout va -Thanh toan-                   | Template Method + (Strategy + Factory Method) |
| 2 - Cuc ky uu tien   | Tinh gia Ve/F&B/Voucher                    | Strategy + Chain of Responsibility            |
| 3 - Rat cao          | -Khoa ghe va trang thai ghe-               | -State + Adapter-                             |
| 4 - Cao              | -Hau xu ly sau thanh toan (email, loyalty) | -Observer (Domain Event)                      |
| 5 - Can nhac mo rong | Quan ly state luong dat ve tren Frontend   | Reducer + Command                             |


---

### [Checkout va Thanh toan - Role User]

**Van de hien tai:**  
`CheckoutServiceImpl` dang gop qua nhieu trach nhiem trong cung mot class/method: tao booking pending, luu F&B, goi MoMo, xu ly callback, tao ticket, cap nhat thanh toan, gui email. Logic xu ly thanh toan dang hard-code theo MoMo, dung nhieu nhanh `if/else` va side effects chen trong transaction flow. Dieu nay vi pham SRP va lam cho viec mo rong them payment method moi (VD: VNPay) de phat sinh sua nhieu noi, nguy co regression cao.

**De xuat Pattern:**  
`Template Method` + `Strategy` + `Factory Method`.

**Ly do lua chon:**  

- `Template Method` giu khung xu ly checkout/callback co dinh (validate -> create pending -> call gateway -> finalize).  
- `Strategy` tach logic theo tung cong thanh toan (`MomoPaymentStrategy`, `VnPayPaymentStrategy`...).  
- `Factory Method` chon dung strategy dua tren `paymentMethod` cua request, tranh hard-code.

**Hieu qua mang lai:**  

- Checkout flow ro rang, giam coupling giua use-case va cong thanh toan cu the.  
- De unit test theo tung strategy va tung buoc cua template (mock gateway, mock repository).  
- Them payment method moi theo Open/Closed: them class moi, han che sua code cu.

**Danh sach File can thay doi:**  

- **File hien co can sua:**
  - `backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/controllers/PaymentController.java`
  - `backend/src/main/java/com/cinema/booking/services/impl/MomoServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/services/CheckoutService.java`
- **File/interface moi nen tao:**
  - `backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategy.java`
  - `backend/src/main/java/com/cinema/booking/services/payment/MomoPaymentStrategy.java`
  - `backend/src/main/java/com/cinema/booking/services/payment/PaymentStrategyFactory.java`
  - `backend/src/main/java/com/cinema/booking/services/payment/CheckoutTemplate.java` (abstract template)

---

### [Tinh gia Ve/F&B/Voucher - Role User]

**Van de hien tai:**  
Cong thuc tinh gia dang phan tan va co lap logic (gia ve xuat hien o `BookingServiceImpl` va callback trong `CheckoutServiceImpl`). Rule giam gia DB/Redis chia nhanh trong cung method, gay kho doc, kho bo sung rule moi (membership, combo, happy-hour) va kho test theo tung chinh sach.

**De xuat Pattern:**  
`Strategy` + `Chain of Responsibility` (multi-pattern).

**Ly do lua chon:**  

- `Strategy` chia nho bai toan thanh cac policy doc lap: ticket pricing, F&B pricing, discount pricing.  
- `Chain of Responsibility` tao pipeline discount linh hoat: `PromotionDbHandler -> VoucherRedisHandler -> LoyaltyHandler (tuong lai)`.  
- Ket hop 2 pattern giup quy tac gia duoc mo rong theo chieu ngang, khong dong chut logic vao mot method lon.

**Hieu qua mang lai:**  

- Loai bo duplication cong thuc tinh gia, dong bo ket qua giua pre-checkout va callback finalize.  
- Unit test rat de viet: test moi handler/policy doc lap, test toan bo chain theo scenario.  
- Chinh sach khuyen mai thay doi nhanh ma khong can sua code o nhieu module.

**Danh sach File can thay doi:**  

- **File hien co can sua:**
  - `backend/src/main/java/com/cinema/booking/services/impl/BookingServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/dtos/PriceBreakdownDTO.java`
- **File/interface moi nen tao:**
  - `backend/src/main/java/com/cinema/booking/services/pricing/TicketPricingStrategy.java`
  - `backend/src/main/java/com/cinema/booking/services/pricing/FnbPricingStrategy.java`
  - `backend/src/main/java/com/cinema/booking/services/pricing/DiscountHandler.java`
  - `backend/src/main/java/com/cinema/booking/services/pricing/PromotionDiscountHandler.java`
  - `backend/src/main/java/com/cinema/booking/services/pricing/VoucherDiscountHandler.java`
  - `backend/src/main/java/com/cinema/booking/services/pricing/PricingEngine.java`

---

### [Khoa ghe va trang thai ghe - Role User]

**Van de hien tai:**  
Logic trang thai ghe (`VACANT/PENDING/SOLD`) dang xu ly truc tiep trong service voi du lieu tron tu MySQL + Redis. Quy tac transition state chua duoc dinh nghia tap trung, kho mo rong khi them tinh nang realtime hold extension, anti-race condition, hoac multi-device conflict.

**De xuat Pattern:**  
`State` + `Adapter`.

**Ly do lua chon:**  

- `State` chuan hoa hanh vi theo trang thai ghe va transition hop le.  
- `Adapter` boc `RedisTemplate` thanh lock provider interface (`SeatLockProvider`) de giam phu thuoc cong nghe va de test.  
- Ket hop nay giai quyet dong thoi van de nghiep vu (state) va van de ha tang (redis coupling).

**Hieu qua mang lai:**  

- Flow lock/unlock va resolve seat status nhat quan, de audit bug oversell.  
- De thay the lock backend (Redis cluster/distributed lock service) ma it anh huong business code.  
- Unit test de dang voi fake adapter, khong can phu thuoc Redis that.

**Danh sach File can thay doi:**  

- **File hien co can sua:**
  - `backend/src/main/java/com/cinema/booking/services/impl/BookingServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/dtos/SeatStatusDTO.java`
  - `backend/src/main/java/com/cinema/booking/config/RedisConfig.java`
- **File/interface moi nen tao:**
  - `backend/src/main/java/com/cinema/booking/domain/seat/SeatState.java`
  - `backend/src/main/java/com/cinema/booking/domain/seat/VacantSeatState.java`
  - `backend/src/main/java/com/cinema/booking/domain/seat/PendingSeatState.java`
  - `backend/src/main/java/com/cinema/booking/domain/seat/SoldSeatState.java`
  - `backend/src/main/java/com/cinema/booking/services/seatlock/SeatLockProvider.java`
  - `backend/src/main/java/com/cinema/booking/services/seatlock/RedisSeatLockAdapter.java`

---

### [Hau xu ly sau thanh toan (email, loyalty) - Role User]

**Van de hien tai:**  
Trong callback payment, cac side effect nhu gui email va cap nhat loyalty/spending dang goi truc tiep trong flow chinh. Khi mot side effect loi, code phai xu ly bo sung de tranh rollback sai logic, lam callback method dai va kho test.

**De xuat Pattern:**  
`Observer` (Domain Event).

**Ly do lua chon:**  

- Sau khi booking thanh cong, publish event `BookingPaidEvent`.  
- Cac listener doc lap (`TicketEmailListener`, `LoyaltyUpdateListener`) subscribe va xu ly rieng.  
- Pattern nay giam coupling giua transaction cot loi va hau xu ly.

**Hieu qua mang lai:**  

- Callback gon hon, trach nhiem ro rang.  
- De them side effect moi (VD: push notification, analytics) ma khong sua flow chinh.  
- Unit test de tach thanh test publisher va test tung listener.

**Danh sach File can thay doi:**  

- **File hien co can sua:**
  - `backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/services/impl/EmailServiceImpl.java`
  - `backend/src/main/java/com/cinema/booking/services/impl/UserServiceImpl.java` (neu co update loyalty tai day)
- **File/interface moi nen tao:**
  - `backend/src/main/java/com/cinema/booking/events/BookingPaidEvent.java`
  - `backend/src/main/java/com/cinema/booking/events/BookingPaidEventPublisher.java`
  - `backend/src/main/java/com/cinema/booking/events/listeners/TicketEmailListener.java`
  - `backend/src/main/java/com/cinema/booking/events/listeners/LoyaltyUpdateListener.java`

---

### [Quan ly state luong dat ve tren Frontend - Role User]

**Van de hien tai:**  
State booking user dang vua dung context, vua map thu cong tai nhieu page (`Home`, `MovieList`, `CinemaDetails`, `SeatSelection`, `Payment`). Co lap logic mapping selection va buoc flow, de gay lech state giua cac man hinh va kho theo doi regressions.

**De xuat Pattern:**  
`Reducer` + `Command` (multi-pattern).

**Ly do lua chon:**  

- `Reducer` tao "single source of truth" cho state transition (`SELECT_SHOWTIME`, `SELECT_SEATS`, `APPLY_VOUCHER`, `CHECKOUT_REQUEST`...).  
- `Command` dong goi user actions thanh cac command co the tai su dung (`ApplyVoucherCommand`, `SubmitCheckoutCommand`).  
- `Command` se goi service layer, `Reducer` quan ly state; hai pattern bo tro nhau ro rang.

**Hieu qua mang lai:**  

- Giam duplicate logic giua cac page booking.  
- De unit test: test reducer thuần + test command voi mocked API.  
- Code UI de bao tri hon khi thay doi thu tu step hoac bo sung step moi.

**Danh sach File can thay doi:**  

- **File hien co can sua:**
  - `frontend/src/contexts/BookingContext.jsx`
  - `frontend/src/pages/Home.jsx`
  - `frontend/src/pages/MovieList.jsx`
  - `frontend/src/pages/CinemaDetails.jsx`
  - `frontend/src/pages/SeatSelection.jsx`
  - `frontend/src/pages/Payment.jsx`
  - `frontend/src/services/bookingService.js`
  - `frontend/src/services/paymentService.js`
- **File/interface moi nen tao:**
  - `frontend/src/booking/bookingReducer.js`
  - `frontend/src/booking/bookingActionTypes.js`
  - `frontend/src/booking/commands/ApplyVoucherCommand.js`
  - `frontend/src/booking/commands/SubmitCheckoutCommand.js`
  - `frontend/src/booking/commands/SelectSeatsCommand.js`

---

## Ghi chu tranh over-engineering

- Khong ap dung pattern cho CRUD don gian khong thuoc user booking flow.  
- Chua can Event Bus/Kafka/Outbox o giai doan nay; Spring event noi bo la du.  
- Chua can state machine framework ben ngoai; enum + interface + transition ro rang da du dung.  
- Uu tien lam theo thu tu trong tai lieu de toi uu ROI va han che rui ro refactor lon.

