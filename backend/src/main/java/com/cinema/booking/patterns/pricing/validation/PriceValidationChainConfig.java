package com.cinema.booking.patterns.pricing.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Xây dựng PriceValidationChain theo thứ tự:
 * MinPrice → MaxDiscount → FraudDetection
 *
 * <p>Thứ tự này có chủ ý:
 * <ol>
 *   <li>MinPrice trước: nếu finalTotal âm thì MaxDiscount sẽ tính tỷ lệ sai</li>
 *   <li>MaxDiscount sau: chỉ chạy khi đã biết finalTotal hợp lệ (dương)</li>
 *   <li>FraudDetection cuối: chỉ chạy khi giá đã vượt qua 2 check trên</li>
 * </ol>
 * </p>
 *
 * Bean name {@code priceValidationChain} không conflict với {@code checkoutValidationChain}.
 */
@Configuration
public class PriceValidationChainConfig {

    @Bean
    public PriceValidationHandler priceValidationChain(
            MinPriceHandler minPrice,
            MaxDiscountHandler maxDiscount,
            FraudDetectionHandler fraudDetection) {

        minPrice.setNext(maxDiscount);
        maxDiscount.setNext(fraudDetection);
        // fraudDetection là cuối chuỗi, next = null
        return minPrice;
    }
}
