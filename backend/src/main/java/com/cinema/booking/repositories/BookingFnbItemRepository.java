package com.cinema.booking.repositories;

import com.cinema.booking.entities.BookingFnbItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingFnbItemRepository extends JpaRepository<BookingFnbItem, Integer> {
    List<BookingFnbItem> findByBooking_BookingId(Integer bookingId);
}
