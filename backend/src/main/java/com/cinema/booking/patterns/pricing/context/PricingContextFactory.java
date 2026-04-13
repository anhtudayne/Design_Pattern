package com.cinema.booking.patterns.pricing.context;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.FnbItem;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.Showtime;
import com.cinema.booking.repositories.CustomerRepository;
import com.cinema.booking.repositories.FnbItemRepository;
import com.cinema.booking.repositories.PromotionRepository;
import com.cinema.booking.repositories.SeatRepository;
import com.cinema.booking.repositories.ShowtimeRepository;
import com.cinema.booking.repositories.TicketRepository;
import com.cinema.booking.security.UserDetailsImpl;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory (SRP) — chuyển BookingCalculationDTO + DB thành PricingContext.
 * Không có bất kỳ business logic giá nào ở đây.
 *
 * Hai overload build() hỗ trợ cả HTTP request lẫn background job:
 *  - build(dto)          → dùng cho HTTP request: lấy userId từ SecurityContextHolder
 *  - build(dto, userId)  → dùng cho background job: truyền userId tường minh
 */
@Component
@RequiredArgsConstructor
public class PricingContextFactory {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final FnbItemRepository fnbItemRepository;
    private final PromotionRepository promotionRepository;
    private final CustomerRepository customerRepository;

    /**
     * Dùng cho HTTP request: lấy userId từ SecurityContextHolder.
     */
    public PricingContext build(BookingCalculationDTO request) {
        return build(request, resolveCurrentUserId());
    }

    /**
     * Dùng cho background job / cron / async task:
     * truyền userId tường minh thay vì phụ thuộc SecurityContextHolder.
     * Ví dụ: scheduled price recalculation, batch pricing audit.
     */
    public PricingContext build(BookingCalculationDTO request, @Nullable Integer userId) {
        // 1. Load showtime
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Suất chiếu không tồn tại: id=" + request.getShowtimeId()));

        // 2. Load seats
        List<Seat> seats = request.getSeatIds().stream()
                .map(seatId -> seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Ghế không hợp lệ: id=" + seatId)))
                .toList();

        // 3. Resolve promotion (null nếu blank / không tìm thấy / hết hạn)
        Promotion promotion = resolvePromotion(request.getPromoCode());

        // 4. Resolve customer (null nếu anonymous)
        Customer customer = userId != null
                ? customerRepository.findById(userId).orElse(null)
                : null;

        // 5. Compute fnbTotal: sum(fnbItem.price * qty)
        BigDecimal fnbTotal = computeFnbTotal(request);

        // 6. Count bookedSeatsCount
        int bookedSeatsCount = (int) ticketRepository.countByShowtime_ShowtimeId(
                showtime.getShowtimeId());

        // 7. Count totalSeatsCount
        int totalSeatsCount = (int) seatRepository.countByRoom_RoomId(
                showtime.getRoom().getRoomId());

        // 8. Build và return PricingContext
        return new PricingContext(
                showtime,
                seats,
                customer,
                promotion,
                fnbTotal,
                bookedSeatsCount,
                totalSeatsCount,
                LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Resolve userId từ SecurityContextHolder (cho HTTP flow). */
    @Nullable
    private Integer resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return ud.getId();
        }
        return null; // anonymous
    }

    /**
     * Resolve promotion: trả về null nếu promoCode blank, không tồn tại, hoặc đã hết hạn.
     */
    @Nullable
    private Promotion resolvePromotion(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) return null;
        return promotionRepository.findByCode(promoCode.trim())
                .filter(p -> p.getValidTo().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    /** Tính tổng tiền F&B từ danh sách FnbOrderDTO. */
    private BigDecimal computeFnbTotal(BookingCalculationDTO request) {
        if (request.getFnbs() == null || request.getFnbs().isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BookingCalculationDTO.FnbOrderDTO order : request.getFnbs()) {
            FnbItem item = fnbItemRepository.findById(order.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Món F&B không tồn tại: id=" + order.getItemId()));
            total = total.add(item.getPrice().multiply(
                    BigDecimal.valueOf(order.getQuantity())));
        }
        return total;
    }
}
