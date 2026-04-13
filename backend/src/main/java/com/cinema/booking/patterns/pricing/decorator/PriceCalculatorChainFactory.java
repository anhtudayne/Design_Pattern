package com.cinema.booking.patterns.pricing.decorator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory xây dựng chuỗi Decorator cố định mỗi request.
 *
 * <p>Thứ tự: Base → Occupancy → Fnb → MemberDiscount → Voucher</p>
 * <ul>
 *   <li>OccupancyDecorator: tính surcharge trên ticketTotal — phải chạy trước F&B</li>
 *   <li>FnbDecorator: thêm F&B vào subtotal</li>
 *   <li>MemberDiscountDecorator: giảm trên ticketTotal (chỉ vé, không giảm F&B)</li>
 *   <li>VoucherDecorator: giảm trên phần còn lại sau membership</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class PriceCalculatorChainFactory {

    private final BasePriceCalculator base;

    /**
     * Trả về chuỗi decorator hoàn chỉnh sẵn sàng để gọi {@code calculate(ctx)}.
     * Mỗi request tạo instance mới — Decorator là POJO, không phải Spring bean.
     */
    public PriceCalculator buildChain() {
        return new VoucherDecorator(
                new MemberDiscountDecorator(
                        new FnbDecorator(
                                new OccupancyDecorator(base)
                        )
                )
        );
    }
}
