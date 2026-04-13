package com.cinema.booking.services.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory Method: chọn {@link PaymentStrategy} theo {@link PaymentMethod}.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    @Autowired
    public PaymentStrategyFactory(List<PaymentStrategy> strategyBeans) {
        for (PaymentStrategy strategy : strategyBeans) {
            PaymentMethod key = strategy.getPaymentMethod();
            if (strategies.put(key, strategy) != null) {
                throw new IllegalStateException("Trùng PaymentStrategy cho: " + key);
            }
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (!strategies.containsKey(method)) {
                throw new IllegalStateException("Thiếu PaymentStrategy cho: " + method);
            }
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Chưa đăng ký strategy cho: " + method);
        }
        return strategy;
    }

    public <T extends PaymentStrategy> T getStrategy(PaymentMethod method, Class<T> type) {
        PaymentStrategy strategy = getStrategy(method);
        if (!type.isInstance(strategy)) {
            throw new IllegalStateException("Strategy " + method + " không phải " + type.getSimpleName());
        }
        return type.cast(strategy);
    }
}
