# Plan chi tiet — Chain of Responsibility (Checkout validation)

**Muc tieu:** Tach validation trong `CheckoutServiceImpl.createBooking()` thanh chuoi handler doc lap, de mo rong rule moi ma khong sua mot method qua lon.

**File hien co:** `backend/src/main/java/com/cinema/booking/services/impl/CheckoutServiceImpl.java`

**Package moi de xuat:** `com.cinema.booking.patterns.chainofresponsibility`

---

## Buoc 0 — Chuan bi

1. Doc toan bo `createBooking()` va liet ke tung dieu kien hien co (user, showtime, ghe da ban, promo neu co).
2. Quyet dinh **context** dung chung cho chain: vi du `CheckoutValidationContext` chua `userId`, `showtimeId`, `seatIds`, `promoCode`, va cac field sau khi pass (User, Showtime, …) neu can tranh query lap.

---

## Buoc 1 — Dinh nghia interface handler

1. Tao `CheckoutValidationHandler` (abstract hoac interface) voi:
   - `void setNext(CheckoutValidationHandler next);`
   - `void handle(CheckoutValidationContext ctx);` hoac tra `Optional<String>` / nem `RuntimeException` thong nhat voi codebase.
2. Thong nhat **cach bao loi**: giu message tieng Viet nhu hien tai (`RuntimeException`).

---

## Buoc 2 — Implement tung handler (map 1-1 voi logic cu)

Thu tu de xuat (co the doi de fail-fast):

| Handler | Trach nhiem | Ghi chu |
|---------|-------------|---------|
| `MaxSeatsHandler` | `seatIds == null` hoac rong → loi; `seatIds.size() > 8` → loi ro rang | **Bat buoc** theo yeu cau nghiep vu |
| `UserExistsHandler` | `userRepository.findById(userId)` | Giu message: "Nguoi dung khong ton tai" |
| `ShowtimeExistsHandler` | `showtimeRepository.findById(showtimeId)` | Giu message: "Suat chieu khong ton tai" |
| `SeatsNotSoldHandler` | Vong lap `ticketRepository.existsByBooking_Showtime_ShowtimeIdAndSeat_SeatId` | Giu message ghe da ban |
| `PromoOptionalHandler` (neu can) | Chi validate khi co `promoCode` — chi them neu hien tai dang validate o day; neu promo chi dung khi tinh gia, co the bo qua chain | Tranh trung logic voi `BookingServiceImpl.calculatePrice` |

Sau moi handler: neu OK thi goi `next.handle(ctx)`.

---

## Buoc 3 — Factory / builder chuoi

1. Tao `CheckoutValidationChainFactory` hoac method static `buildDefaultChain(...dependencies)` inject repository can thiet.
2. Noi chuoi: `h1.setNext(h2); ... setNext(hLast);`
3. **Khong** de `CheckoutServiceImpl` tu `new` tung handler neu dung Spring: danh dau handler la `@Component` hoac tao `@Bean` chain trong `config`.

---

## Buoc 4 — Refactor `CheckoutServiceImpl.createBooking()`

1. Xoa/trim cac block validation cu.
2. Dau method: tao `context`; `validationChain.handle(context);`
3. Phan con lai (tinh gia, luu booking, F&B, MoMo) giu nguyen thu tu nghiep vu.

---

## Buoc 5 — Kiem thu

1. **Happy path:** 1–8 ghe, user/showtime hop le.
2. **Max seats:** gui 9 ghe → loi ro rang.
3. **Ghe da ban:** giu hanh vi cu.
4. Regression: response API `/api/payment/checkout` khong doi contract.

---

## Rui ro & luu y

- Tranh query DB trung: co the cache `User`/`Showtime` tren `context` sau handler dau tien.
- Neu them rule "showtime da chieu xong", them handler moi **khong sua** handler cu.

---

## Checklist hoan thanh

- [ ] `MaxSeatsHandler` enforce toi da 8 ghe
- [ ] Toan bo validation cu da chuyen vao chain
- [ ] `createBooking()` gon, chi dieu phoi
- [ ] Build/test backend pass
