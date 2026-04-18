package com.cinema.booking.repository;

import com.cinema.booking.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer>, JpaSpecificationExecutor<Showtime> {
    List<Showtime> findByRoom_RoomIdAndStartTimeBetween(Integer roomId, LocalDateTime start, LocalDateTime end);
}
