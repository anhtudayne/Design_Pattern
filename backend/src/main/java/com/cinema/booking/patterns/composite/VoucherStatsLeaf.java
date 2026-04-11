package com.cinema.booking.patterns.composite;

import com.cinema.booking.services.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class VoucherStatsLeaf implements StatsComponent {

    private final VoucherService voucherService;

    @Override
    public void collect(Map<String, Object> target) {
        // Fallback to 0 when Redis is unavailable — preserve existing behaviour
        try {
            target.put("totalVouchers", voucherService.getAllVouchers().size());
        } catch (Exception e) {
            target.put("totalVouchers", 0);
        }
    }
}
