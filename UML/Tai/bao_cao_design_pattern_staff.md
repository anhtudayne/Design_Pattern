# BÁO CÁO PHÂN TÍCH DESIGN PATTERN — ROLE STAFF
## Hệ thống Quản lý Rạp Chiếu Phim (Cinema Booking System)

---

> [!IMPORTANT]
> **Phạm vi báo cáo:** Tài liệu này tập trung vào **4 Design Pattern** được áp dụng trực tiếp vào luồng nghiệp vụ của **Nhân viên (Staff)**, bao gồm: bán vé tại quầy (POS), quản lý bắp nước (F&B Concession), tra cứu đơn hàng và quản lý vòng đời booking.

---

## I. ĐẶT VẤN ĐỀ — TẠI SAO CẦN DESIGN PATTERN?

### 1.1. Bối cảnh nghiệp vụ của Staff

Trong hệ thống rạp chiếu phim, nhân viên (Staff) phải xử lý song song nhiều luồng công việc phức tạp:

| Nghiệp vụ | Mô tả | Thách thức kỹ thuật |
|---|---|---|
| **Bán vé tại quầy (Box Office POS)** | Chọn ghế, thêm bắp nước, tính tiền, in vé cho khách vãng lai | Giao diện phải nhanh, có thể Undo nếu chọn nhầm |
| **Quầy F&B Concession** | Bán đồ ăn/nước uống riêng biệt, không gắn với vé | Kiểm soát tồn kho real-time |
| **Tra cứu & Quản lý Đơn hàng** | Tìm kiếm đơn theo mã, SĐT, email | Tìm kiếm linh hoạt, nhiều tiêu chí |
| **Chuyển trạng thái Booking** | Xác nhận, Hủy, Hoàn tiền cho đơn hàng | Ngăn chặn chuyển đổi trạng thái sai quy trình |

### 1.2. Vấn đề nếu không dùng Design Pattern

```
❌ Không có Design Pattern:

checkout() {
  if (paymentType == "CASH") {
    // 50 dòng code xử lý tiền mặt
  } else if (paymentType == "MOMO") {  
    // 60 dòng code gọi API MoMo
  }
  
  if (status == "PENDING") {
    if (action == "CANCEL") { ... }
    else if (action == "CONFIRM") { ... }
    else if (action == "REFUND") { ... error }
  } else if (status == "CONFIRMED") { ... }
  // → Chuỗi if-else vô tận, dễ bug
}
```

**Hậu quả cụ thể:**
- **Thiếu Undo/Redo:** Nhân viên chọn nhầm ghế → phải làm lại từ đầu, gây mất thời gian và thất vọng.
- **Thanh toán tràn lan:** Thêm phương thức mới (VNPay) phải sửa hàm `checkout()` 300 dòng → rủi ro cao.
- **Trạng thái hỗn loạn:** Đơn hàng đã hủy vẫn có thể bị "Hoàn tiền" → sai lệch tài chính nghiêm trọng.
- **Tìm kiếm cứng nhắc:** Chỉ tìm được theo 1 tiêu chí → trải nghiệm Staff tệ.

---

## II. CÁC DESIGN PATTERN ÁP DỤNG

---

### 🟠 PATTERN 1: COMMAND PATTERN
**Phạm vi:** Frontend — Module Box Office POS  
**File:** `frontend/src/patterns/posCommands.js`

#### 2.1.1. Lý thuyết

**Command Pattern** đóng gói một hành động (như "thêm ghế A3") thành một **đối tượng độc lập**. Đối tượng này chứa đủ thông tin để:
- **Thực thi** hành động (`execute()`)
- **Hoàn tác** hành động (`undo()`)
- **Mô tả** hành động (`describe()`)

```
┌─────────────┐    execute()     ┌──────────────────┐
│   Invoker   │ ───────────────→ │   Command Object │
│(PosInvoker) │    undo()        │(AddSeatCommand)  │
│             │ ←─────────────── │                  │
└─────────────┘                  └──────────────────┘
                                          │
                                          ▼ tác động lên
                                  ┌──────────────────┐
                                  │    Receiver      │
                                  │ (React State)    │
                                  └──────────────────┘
```

**3 thành phần chính áp dụng trong dự án:**

| Thành phần | Pattern Role | Code tương ứng |
|---|---|---|
| `Command` (interface) | Command | Class `Command` với `execute()`, `undo()`, `describe()` |
| `AddSeatCommand`, `RemoveSeatCommand`, `AddFnbCommand`, `RemoveFnbCommand` | ConcreteCommand | 4 class kế thừa `Command` |
| `PosCommandInvoker` | Invoker | Quản lý `history[]` và `redoStack[]` |

#### 2.1.2. Code thực tế trong dự án

```javascript
// ── Command Interface ─────────────────────────────────────────────────
class Command {
  execute() { throw new Error('execute() not implemented'); }
  undo()    { throw new Error('undo() not implemented');    }
  describe(){ return 'Command'; }
}

// ── Concrete Command: Thêm ghế ────────────────────────────────────────
export class AddSeatCommand extends Command {
  constructor(seat, setSelectedSeats) {
    super();
    this.seat = seat;
    this.setSelectedSeats = setSelectedSeats;
  }
  execute() {   // → Thêm ghế vào danh sách
    this.setSelectedSeats(prev => {
      if (prev.find(s => s.seatId === this.seat.seatId)) return prev;
      if (prev.length >= 10) return prev;
      return [...prev, this.seat];
    });
  }
  undo() {      // → Xóa ghế vừa thêm
    this.setSelectedSeats(prev => 
      prev.filter(s => s.seatId !== this.seat.seatId)
    );
  }
  describe() { return `Thêm ghế ${this.seat.seatRow}${this.seat.seatNumber}`; }
}

// ── Invoker: Quản lý History Stack ───────────────────────────────────
export class PosCommandInvoker {
  constructor() {
    this.history = [];    // Stack lưu lệnh đã thực hiện
    this.redoStack = [];  // Stack lưu lệnh đã Undo
  }
  execute(command) {
    command.execute();
    this.history.push(command);
    this.redoStack = []; // Clear redo khi có hành động mới
    return command.describe();
  }
  undo() {
    if (!this.canUndo()) return null;
    const command = this.history.pop();
    command.undo();
    this.redoStack.push(command);
    return `Đã hoàn tác: ${command.describe()}`;
  }
  redo() {
    if (!this.canRedo()) return null;
    const command = this.redoStack.pop();
    command.execute();
    this.history.push(command);
    return `Đã thực hiện lại: ${command.describe()}`;
  }
}
```

#### 2.1.3. Lợi ích đạt được

- ✅ **Undo/Redo hoàn chỉnh:** Nhân viên chọn nhầm ghế → bấm Undo → ghế biến mất, không cần làm lại từ đầu.
- ✅ **Tách biệt UI và Logic:** Nút bấm chỉ gọi `invoker.execute(new AddSeatCommand(...))`, không biết gì về logic cộng/trừ ghế.
- ✅ **Mở rộng dễ dàng:** Muốn thêm Command mới (ví dụ `ApplyDiscountCommand`) → kế thừa từ `Command`, không đụng đến code cũ.

---

### 🔵 PATTERN 2: TEMPLATE METHOD PATTERN
**Phạm vi:** Backend — Luồng Thanh toán tại quầy  
**File:** `backend/.../template_method/checkout/AbstractCheckoutTemplate.java`  
**Subclass Staff:** `StaffCashCheckoutProcess.java`

#### 2.2.1. Lý thuyết

**Template Method Pattern** định nghĩa **khung sườn** (Skeleton) của một thuật toán trong lớp cha. Các bước cụ thể được ủy quyền cho lớp con override.

```
AbstractCheckoutTemplate
├── checkout() [final - không cho override]
│    ├── validateUser()           [step 1 - shared]
│    ├── validateSeats()          [step 2 - shared]
│    ├── calculatePrice()         [step 3 - shared]
│    ├── findPromotion()          [step 4 - shared]
│    ├── determineInitialStatus() [step 5 - ABSTRACT]
│    ├── createBooking()          [step 6 - shared]
│    ├── saveFnbLines()           [step 7 - shared]
│    ├── processPayment()         [step 8 - ABSTRACT]
│    └── finalizeBooking()        [step 9 - ABSTRACT]
│
├── StaffCashCheckoutProcess  → tiền mặt, CONFIRMED ngay
├── MomoCheckoutProcess       → gọi API MoMo, PENDING chờ callback
└── DemoCheckoutProcess       → demo/test mode
```

#### 2.2.2. Code thực tế trong dự án

**Lớp cha — Khung sườn thuật toán:**
```java
// AbstractCheckoutTemplate.java
public abstract class AbstractCheckoutTemplate {

    @Transactional
    public final CheckoutResult checkout(CheckoutRequest request) throws Exception {
        Customer customer = validateUser(request.getUserId());         // Chung
        validateSeats(request.getShowtimeId(), request.getSeatIds());  // Chung
        PriceBreakdownDTO price = calculatePrice(request);             // Chung
        Promotion promotion = findPromotion(request.getPromoCode());   // Chung

        // ↓ Bước này KHÁC nhau tùy loại thanh toán
        Booking.BookingStatus status = determineInitialBookingStatus(request);

        Booking booking = createBooking(customer, promotion, status);  // Chung
        saveFnbLines(booking, request.getFnbs());                      // Chung

        // ↓ Bước này KHÁC nhau (Cash vs MoMo vs Demo)
        Object paymentResult = processPayment(booking, price, request);

        // ↓ Bước này KHÁC nhau (tạo Ticket, cập nhật spending...)
        finalizeBooking(booking, price, request, paymentResult);

        return CheckoutResult.builder()...build();
    }

    // Các bước được ủy quyền cho subclass định nghĩa:
    protected abstract Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request);
    protected abstract Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request);
    protected abstract void finalizeBooking(Booking booking, PriceBreakdownDTO price, CheckoutRequest request, Object paymentResult);
}
```

**Lớp con — Dành riêng cho Staff thanh toán Tiền mặt:**
```java
// StaffCashCheckoutProcess.java
@Component
public class StaffCashCheckoutProcess extends AbstractCheckoutTemplate {

    // Bước 5: Staff nhận tiền → Xác nhận NGAY, không cần chờ callback
    @Override
    protected Booking.BookingStatus determineInitialBookingStatus(CheckoutRequest request) {
        return Booking.BookingStatus.CONFIRMED; // ← Đặc trưng của Staff
    }

    // Bước 8: Tạo bản ghi thanh toán tiền mặt, KHÔNG gọi API ngoài
    @Override
    protected Object processPayment(Booking booking, PriceBreakdownDTO price, CheckoutRequest request) {
        Payment payment = bookingFactory.createPayment(
            booking, "CASH", price.getFinalTotal(), Payment.PaymentStatus.SUCCESS
        );
        paymentRepository.save(payment);
        return payment; // SUCCESS ngay lập tức
    }

    // Bước 9: Tạo Ticket cho từng ghế, cập nhật totalSpending khách
    @Override
    protected void finalizeBooking(...) {
        for (Integer seatId : request.getSeatIds()) {
            Ticket ticket = bookingFactory.createTicket(booking, seat, showtime, ticketPrice);
            ticketRepository.save(ticket);
        }
        safeIncreaseCustomerSpending(booking.getCustomer().getUserId(), price.getFinalTotal());
    }
}
```

#### 2.2.3. So sánh Staff vs Online Checkout

| Bước | `StaffCashCheckoutProcess` | `MomoCheckoutProcess` |
|---|---|---|
| `determineInitialBookingStatus` | `CONFIRMED` (nhận tiền xong luôn) | `PENDING` (chờ MoMo callback) |
| `processPayment` | Lưu `CASH/SUCCESS` vào DB | Gọi API MoMo, lấy `payUrl` |
| `finalizeBooking` | Tạo vé + cập nhật chi tiêu | Chỉ lưu payment, chờ webhook |

#### 2.2.4. Lợi ích đạt được

- ✅ **Tái sử dụng:** 6/9 bước checkout (validate, tính tiền, lưu F&B...) được dùng chung, không viết lại.
- ✅ **Dễ mở rộng:** Thêm thanh toán VNPay → chỉ tạo class `VNPayCheckoutProcess`, override 3 phương thức abstract.
- ✅ **Tính nhất quán:** Dù Staff hay Online, luôn phải validate user, kiểm tra ghế → không bao giờ bỏ sót bước quan trọng.

---

### 🟢 PATTERN 3: STATE PATTERN
**Phạm vi:** Backend — Vòng đời Booking  
**Files:** `BookingState.java`, `BookingContext.java`, `PendingState.java`, `ConfirmedState.java`, `CancelledState.java`, `RefundedState.java`

#### 2.3.1. Lý thuyết

**State Pattern** cho phép một đối tượng thay đổi **hành vi** của nó khi trạng thái nội tại thay đổi. Thay vì dùng chuỗi `if-else status == X`, mỗi trạng thái là một class riêng tự biết mình được phép làm gì.

```
BookingState (Interface)
├── confirm(context)
├── cancel(context)
├── printTickets(context)
└── refund(context)

Implementations:
├── PendingState    → confirm/cancel được; printTickets/refund ném lỗi
├── ConfirmedState  → cancel/refund được; confirm ném lỗi
├── CancelledState  → TẤT CẢ đều ném lỗi (trạng thái cuối)
└── RefundedState   → TẤT CẢ đều ném lỗi (trạng thái cuối)
```

**Sơ đồ chuyển đổi trạng thái:**
```
           ┌─────────┐
           │ PENDING │
           └────┬────┘
    confirm()   │      cancel()
                ▼         ▼
          ┌──────────┐  ┌───────────┐
          │CONFIRMED │  │ CANCELLED │ ← (trạng thái kết thúc)
          └────┬─────┘  └───────────┘
  cancel() /   │ refund()
  refund()     ▼
          ┌───────────┐
          │ CANCELLED │ ← (trạng thái kết thúc)
          └───────────┘
```

#### 2.3.2. Code thực tế trong dự án

**Interface định nghĩa hành động:**
```java
// BookingState.java
public interface BookingState {
    void confirm(BookingContext context);
    void cancel(BookingContext context);
    void printTickets(BookingContext context);
    void refund(BookingContext context);
    String getStateName();
}
```

**Context — Cầu nối giữa Staff và trạng thái:**
```java
// BookingContext.java
public class BookingContext {
    private BookingState state;
    private final Booking booking;

    public BookingContext(Booking booking) {
        this.booking = booking;
        // Tự động khôi phục đúng trạng thái từ DB
        this.state = StateFactory.getState(booking.getStatus());
    }

    public void setState(BookingState state) {
        this.state = state;
        // Đồng bộ trạng thái về Entity trong DB
        this.booking.setStatus(Booking.BookingStatus.valueOf(state.getStateName()));
    }

    // Staff gọi những phương thức này:
    public void confirm()      { state.confirm(this);      }
    public void cancel()       { state.cancel(this);       }
    public void printTickets() { state.printTickets(this); }
    public void refund()       { state.refund(this);       }
}
```

**Ví dụ: Hành vi khi ở trạng thái CONFIRMED:**
```java
// ConfirmedState.java
public class ConfirmedState implements BookingState {
    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được xác nhận từ trước!");
    }
    @Override
    public void cancel(BookingContext context) {
        context.setState(new CancelledState()); // Hủy hợp lệ
    }
    @Override
    public void refund(BookingContext context) {
        context.setState(new CancelledState()); // Hoàn tiền hợp lệ
    }
    @Override
    public String getStateName() { return "CONFIRMED"; }
}
```

**Ví dụ: Hành vi khi ở trạng thái CANCELLED:**
```java
// CancelledState.java
public class CancelledState implements BookingState {
    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã hủy, không thể xác nhận.");
    }
    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã ở trạng thái hủy.");
    }
    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Đơn hàng bị hủy khi chưa thanh toán, không có chi phí để hoàn.");
    }
    @Override
    public String getStateName() { return "CANCELLED"; }
}
```

#### 2.3.3. Ma trận chuyển đổi trạng thái

| Trạng thái hiện tại | `confirm()` | `cancel()` | `printTickets()` | `refund()` |
|:---:|:---:|:---:|:---:|:---:|
| **PENDING** | ✅ → CONFIRMED | ✅ → CANCELLED | ❌ Lỗi | ❌ Lỗi |
| **CONFIRMED** | ❌ Lỗi | ✅ → CANCELLED | ✅ In vé | ✅ → CANCELLED |
| **CANCELLED** | ❌ Lỗi | ❌ Lỗi | ❌ Lỗi | ❌ Lỗi |
| **REFUNDED** | ❌ Lỗi | ❌ Lỗi | ❌ Lỗi | ❌ Lỗi |

#### 2.3.4. Lợi ích đạt được

- ✅ **An toàn nghiệp vụ:** Không thể Hoàn tiền đơn chưa thanh toán, không thể Hủy đơn đã hủy.
- ✅ **Tự tài liệu hóa:** Đọc class `ConfirmedState` là hiểu ngay đơn `CONFIRMED` được làm gì và không được làm gì.
- ✅ **Loại bỏ if-else:** Thay vì viết `if (status == CONFIRMED && action == REFUND)`, chỉ cần gọi `context.refund()`.

---

### 🟣 PATTERN 4: SPECIFICATION PATTERN
**Phạm vi:** Backend — Tra cứu đơn hàng tại trang Order Lookup  
**File:** `backend/.../patterns/specification/BookingSpecificationBuilder.java`

#### 2.4.1. Lý thuyết

**Specification Pattern** đóng gói **điều kiện lọc/tìm kiếm** thành một đối tượng riêng, có thể kết hợp linh hoạt (`AND`, `OR`). Trong Spring, nó tích hợp với `JpaSpecificationExecutor` để tạo câu SQL động mà không viết SQL thủ công.

```
Staff gõ vào ô tìm kiếm: "0987654321"
           ↓
BookingSpecificationBuilder.searchBookings("0987654321")
           ↓
Tự động tạo Specification:
  WHERE booking.bookingId = ?
     OR booking.bookingCode LIKE '%0987654321%'
     OR customer.phone LIKE '%0987654321%'
           ↓
BookingRepository.findAll(spec, pageable)
           ↓
Kết quả trả về cho Staff
```

#### 2.4.2. Code thực tế trong dự án

```java
// BookingSpecificationBuilder.java
public class BookingSpecificationBuilder {

    public static Specification<Booking> searchBookings(String query) {
        return (root, criteriaQuery, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction(); // Không có filter → lấy tất cả
            }

            String st = query.trim();
            String lowerSt = st.toLowerCase();
            List<Predicate> preds = new ArrayList<>();

            // Tiêu chí 1: Tìm theo ID (nếu input là số nguyên)
            if (st.matches("\\d+")) {
                preds.add(cb.equal(root.get("bookingId"), Integer.valueOf(st)));
            }

            // Tiêu chí 2: Tìm theo Mã Booking Code (Like)
            preds.add(cb.like(cb.lower(root.get("bookingCode")), "%" + lowerSt + "%"));

            // Tiêu chí 3: Tìm theo Số điện thoại khách hàng
            var customerJoin = root.join("customer", JoinType.LEFT);
            preds.add(cb.like(customerJoin.get("phone"), "%" + st + "%"));

            // Tiêu chí 4: Tìm theo Email (chỉ khi input có @ hoặc không phải số)
            if (st.contains("@") || !st.matches("\\d+")) {
                var accountJoin = customerJoin.join("userAccount", JoinType.LEFT);
                preds.add(cb.like(
                    cb.lower(accountJoin.get("email")), "%" + lowerSt + "%"
                ));
            }

            // Ghép tất cả điều kiện bằng OR
            return cb.or(preds.toArray(new Predicate[0]));
        };
    }
}
```

#### 2.4.3. Ví dụ Staff nhập các từ khóa khác nhau

| Nhân viên nhập | Hệ thống tìm kiếm theo |
|---|---|
| `12` | `bookingId = 12` OR `bookingCode LIKE '%12%'` OR `phone LIKE '%12%'` |
| `BOOK-2024` | `bookingCode LIKE '%book-2024%'` |
| `0987654321` | `bookingId = ?` OR `phone LIKE '%0987654321%'` |
| `nguyen@gmail.com` | `bookingCode LIKE '%..%'` OR `email LIKE '%nguyen@gmail.com%'` |

#### 2.4.4. Lợi ích đạt được

- ✅ **Linh hoạt:** 1 ô tìm kiếm xử lý được 4 tiêu chí khác nhau một cách thông minh.
- ✅ **Tích hợp JPA:** Không viết SQL/JPQL thủ công, không có SQL Injection.
- ✅ **Dễ mở rộng:** Muốn thêm tìm theo "Tên phim" → thêm 5 dòng vào Builder, không đụng code khác.

---

## III. SƠ ĐỒ TỔNG HỢP — LUỒNG HOẠT ĐỘNG STAFF

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                                  │
│                                                                          │
│  [BoxOfficePOS.jsx]                                                      │
│   ↓ Nhân viên click chọn ghế                                             │
│   → new AddSeatCommand(seat, setSelectedSeats)   ← COMMAND PATTERN      │
│   → invoker.execute(cmd)  [history stack cập nhật]                       │
│   ↓ Nhân viên bấm UNDO                                                   │
│   → invoker.undo()  [ghế bị remove khỏi list]                           │
│                                                                          │
│  [FnbConcession.jsx]                                                     │
│   → addToCart(item) với kiểm tra stockQuantity > 0                       │
│                                                                          │
├──────────────────────────────────────────────────────────────────────────┤
│                        BACKEND (Spring Boot)                             │
│                                                                          │
│  POST /api/staff/checkout                                                │
│   → StaffCashCheckoutProcess.checkout(request)  ← TEMPLATE METHOD       │
│      1. validateUser()     → xử lý Khách Vãng Lai nếu cần              │
│      2. validateSeats()    → kiểm tra ghế chưa bán                      │
│      3. calculatePrice()   → Strategy Pattern (giảm giá)                │
│      4. determineStatus()  → CONFIRMED (đặc trưng Staff)                │
│      5. processPayment()   → Lưu CASH/SUCCESS vào DB                    │
│      6. finalizeBooking()  → Tạo Ticket + update spending               │
│                                                                          │
│  GET /api/staff/bookings?q=...                                           │
│   → BookingSpecificationBuilder.searchBookings(q) ← SPECIFICATION       │
│   → Tìm theo: ID, Code, SĐT, Email                                      │
│                                                                          │
│  POST /api/staff/bookings/{id}/cancel                                    │
│   → new BookingContext(booking)          ← STATE PATTERN                 │
│   → context.cancel()                                                     │
│   → ConfirmedState.cancel() → setState(new CancelledState())             │
│   → booking.setStatus(CANCELLED)                                         │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## IV. BẢNG TỔNG KẾT

| # | Design Pattern | Loại | Vị trí áp dụng | Vấn đề giải quyết |
|:---:|---|---|---|---|
| 1 | **Command** | Behavioral | Frontend POS | Undo/Redo khi chọn ghế/F&B sai |
| 2 | **Template Method** | Behavioral | Backend Checkout | Chuẩn hóa luồng thanh toán, tái sử dụng các bước chung |
| 3 | **State** | Behavioral | Backend Booking lifecycle | Kiểm soát chuyển đổi trạng thái an toàn |
| 4 | **Specification** | Behavioral | Backend Order Lookup | Tìm kiếm linh hoạt, đa tiêu chí |

> [!TIP]
> **Cả 4 pattern đều thuộc nhóm Behavioral (Hành vi)** — phản ánh đúng bản chất công việc của Staff: các thao tác nghiệp vụ phức tạp, cần kiểm soát chặt chẽ về hành vi và luồng xử lý.

---

## V. GỢI Ý KHI BÁO CÁO

### 5.1. Cấu trúc trình bày (10–15 phút)

1. **Mở đầu (2 phút):** "Chúng em đảm nhiệm module Staff POS — hệ thống bán vé và quản lý đơn hàng tại quầy. Có 4 vấn đề kỹ thuật chính cần giải quyết..."
2. **Phần chính (10 phút):** Trình bày từng Pattern theo cấu trúc: *Vấn đề → Giải pháp → Demo code → Lợi ích*.
3. **Demo live (3 phút):** Mở giao diện POS, chọn ghế, bấm Undo, chỉ vào code. Sau đó chỉ backend log khi thanh toán tiền mặt.

### 5.2. Câu hỏi thường gặp & cách trả lời

| Câu hỏi | Gợi ý trả lời |
|---|---|
| "Tại sao dùng Command mà không dùng trực tiếp setState?" | "setState thuần không có 'bộ nhớ', không biết trước đó đã làm gì. Command lưu lại từng hành động như một transaction, cho phép rollback từng bước." |
| "Template Method khác Strategy ở điểm nào?" | "Template Method cố định *thứ tự* các bước nhưng cho phép thay đổi *nội dung* từng bước. Strategy thay đổi toàn bộ thuật toán. Ở đây dùng TM vì quy trình checkout luôn theo cùng 9 bước, chỉ khác cách xử lý thanh toán." |
| "Tại sao không dùng enum + switch cho State?" | "Switch case phải sửa một hàm chứa tất cả logic khi thêm trạng thái. State Pattern cho phép thêm trạng thái mới bằng cách tạo class mới, không đụng code cũ — tuân thủ nguyên tắc Open/Closed." |
| "Specification Pattern có tăng hiệu năng không?" | "Không trực tiếp, nhưng Spring Data JPA tối ưu câu query tốt hơn so với tự viết SQL String. Lợi thế chính là maintainability và tránh SQL Injection." |

---

*Báo cáo được soạn dựa trên phân tích trực tiếp mã nguồn dự án tại `d:\STUDY\NAM3\Ky_2\HDT\FINAL_PROJECT\Design_Pattern`*
