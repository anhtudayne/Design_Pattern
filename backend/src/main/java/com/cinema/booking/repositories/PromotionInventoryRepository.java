package com.cinema.booking.repositories;

import com.cinema.booking.entities.PromotionInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionInventoryRepository extends JpaRepository<PromotionInventory, Integer> {
    Optional<PromotionInventory> findByPromotion_Id(Integer promotionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pi from PromotionInventory pi where pi.promotion.id = :promotionId")
    Optional<PromotionInventory> findByPromotionIdForUpdate(@Param("promotionId") Integer promotionId);
}
