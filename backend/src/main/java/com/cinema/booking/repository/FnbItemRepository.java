package com.cinema.booking.repository;

import com.cinema.booking.entity.FnbItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FnbItemRepository extends JpaRepository<FnbItem, Integer> {
}
