package com.cinema.booking.services;

import com.cinema.booking.entities.Promotion;

public interface PromotionInventoryService {
    Promotion reservePromotionOrThrow(String promoCode);

    Promotion resolvePromotionForPricing(String promoCode);

    void releasePromotionForBooking(Integer bookingId);
}
