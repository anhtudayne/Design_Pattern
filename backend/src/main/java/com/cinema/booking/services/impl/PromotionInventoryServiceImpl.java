package com.cinema.booking.services.impl;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.PromotionInventory;
import com.cinema.booking.repositories.BookingRepository;
import com.cinema.booking.repositories.PromotionInventoryRepository;
import com.cinema.booking.repositories.PromotionRepository;
import com.cinema.booking.services.PromotionInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionInventoryServiceImpl implements PromotionInventoryService {

    private final PromotionRepository promotionRepository;
    private final PromotionInventoryRepository promotionInventoryRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Promotion reservePromotionOrThrow(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return null;
        }

        Promotion promotion = promotionRepository.findByCode(promoCode)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại: " + promoCode));

        if (promotion.getValidTo() == null || LocalDateTime.now().isAfter(promotion.getValidTo())) {
            throw new RuntimeException("Mã khuyến mãi đã hết hạn: " + promoCode);
        }

        PromotionInventory inventory = promotionInventoryRepository.findByPromotionIdForUpdate(promotion.getId())
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình tồn kho cho mã khuyến mãi: " + promoCode));

        int current = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        if (current <= 0) {
            throw new RuntimeException("Mã khuyến mãi đã hết lượt sử dụng: " + promoCode);
        }

        inventory.setQuantity(current - 1);
        promotionInventoryRepository.save(inventory);
        return promotion;
    }

    @Override
    @Transactional(readOnly = true)
    public Promotion resolvePromotionForPricing(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return null;
        }

        Promotion promotion = promotionRepository.findByCode(promoCode).orElse(null);
        if (promotion == null || promotion.getValidTo() == null || LocalDateTime.now().isAfter(promotion.getValidTo())) {
            return null;
        }

        PromotionInventory inventory = promotionInventoryRepository.findByPromotion_Id(promotion.getId()).orElse(null);
        if (inventory == null || inventory.getQuantity() == null || inventory.getQuantity() <= 0) {
            return null;
        }
        return promotion;
    }

    @Override
    @Transactional
    public void releasePromotionForBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getPromotion() == null) {
            return;
        }
        Promotion promotion = booking.getPromotion();
        PromotionInventory inventory = promotionInventoryRepository.findByPromotionIdForUpdate(promotion.getId())
                .orElse(null);
        if (inventory == null) {
            return;
        }

        int current = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        inventory.setQuantity(current + 1);
        promotionInventoryRepository.save(inventory);
    }
}
