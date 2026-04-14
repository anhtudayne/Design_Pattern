package com.cinema.booking.repositories;

import com.cinema.booking.entities.FnbItemInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FnbItemInventoryRepository extends JpaRepository<FnbItemInventory, Integer> {
    Optional<FnbItemInventory> findByItem_ItemId(Integer itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select fi from FnbItemInventory fi where fi.item.itemId = :itemId")
    Optional<FnbItemInventory> findByItemIdForUpdate(@Param("itemId") Integer itemId);
}
