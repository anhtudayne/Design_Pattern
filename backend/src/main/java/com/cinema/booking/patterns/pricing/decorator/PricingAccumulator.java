package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.dtos.PriceBreakdownDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Mutable internal object tích lũy các khoản tiền trong chuỗi Decorator.
 * BasePriceCalculator tạo instance ban đầu; mỗi Decorator set đúng 1 field của mình.
 * Cuối chuỗi gọi {@link #toDTO()} một lần duy nhất để convert sang DTO immutable.
 */
@Data
public class PricingAccumulator {

    private BigDecimal ticketTotal        = BigDecimal.ZERO;
    private BigDecimal occupancySurcharge = BigDecimal.ZERO;
    private BigDecimal fnbTotal           = BigDecimal.ZERO;
    private BigDecimal membershipDiscount = BigDecimal.ZERO;
    private BigDecimal voucherDiscount    = BigDecimal.ZERO;
    private String appliedStrategy        = "STANDARD";

    /** Tổng trước discount: ticketTotal + occupancySurcharge + fnbTotal */
    public BigDecimal subtotal() {
        return ticketTotal.add(occupancySurcharge).add(fnbTotal);
    }

    /** Tổng giảm giá: membershipDiscount + voucherDiscount */
    public BigDecimal totalDiscount() {
        return membershipDiscount.add(voucherDiscount);
    }

    /** Giá cuối cùng — clamp >= 0 để tránh âm */
    public BigDecimal finalTotal() {
        BigDecimal result = subtotal().subtract(totalDiscount());
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    /** Convert sang DTO sau khi toàn bộ chain đã chạy xong. */
    public PriceBreakdownDTO toDTO() {
        return PriceBreakdownDTO.builder()
                .ticketTotal(ticketTotal)
                .occupancySurcharge(occupancySurcharge)
                .fnbTotal(fnbTotal)
                .membershipDiscount(membershipDiscount)
                .discountAmount(voucherDiscount)
                .appliedStrategy(appliedStrategy)
                .finalTotal(finalTotal())
                .build();
    }
}
