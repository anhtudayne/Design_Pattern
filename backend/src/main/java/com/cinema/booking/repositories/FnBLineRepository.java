package com.cinema.booking.repositories;

import com.cinema.booking.entities.FnBLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FnBLineRepository extends JpaRepository<FnBLine, Integer> {
    List<FnBLine> findByBooking_BookingId(Integer bookingId);
}

