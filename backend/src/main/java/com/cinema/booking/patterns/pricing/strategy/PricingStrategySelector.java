package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.patterns.pricing.context.PricingContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Selector chọn đúng một {@link PricingStrategy} phù hợp với context.
 *
 * <p>OCP: Spring inject TẤT CẢ @Component implement PricingStrategy.
 * Thêm strategy mới = tạo class @Component mới — không cần mở file này.</p>
 *
 * <p>Thuật toán: sort theo priority() tăng dần, lấy strategy đầu tiên
 * có isApplicable(ctx) = true.</p>
 */
@Component
public class PricingStrategySelector {

    /**
     * Danh sách strategy đã sort theo priority tăng dần — immutable, tính một lần khi startup.
     * Spring inject tất cả @Component implement PricingStrategy vào list này.
     */
    private final List<PricingStrategy> sortedStrategies;

    public PricingStrategySelector(List<PricingStrategy> strategies) {
        this.sortedStrategies = strategies.stream()
                .sorted(Comparator.comparingInt(PricingStrategy::priority))
                .toList();
    }

    /**
     * Chọn strategy đầu tiên (priority thấp nhất) có isApplicable = true.
     * StandardPricingStrategy (priority=999, isApplicable=always true) đảm bảo không bao giờ throw
     * trong production — chỉ throw nếu list rỗng hoặc không có fallback.
     *
     * @throws IllegalStateException nếu không tìm thấy strategy phù hợp (không có fallback)
     */
    public PricingStrategy select(PricingContext ctx) {
        return sortedStrategies.stream()
                .filter(strategy -> strategy.isApplicable(ctx))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy PricingStrategy phù hợp — kiểm tra StandardPricingStrategy đã được đăng ký chưa."));
    }
}
