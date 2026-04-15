package com.cinema.booking.repositories;

import com.cinema.booking.entities.FnbItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FnbItemRepository extends JpaRepository<FnbItem, Integer> {

    java.util.List<FnbItem> findByCinema_CinemaId(Integer cinemaId);
}
