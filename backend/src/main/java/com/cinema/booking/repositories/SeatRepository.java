package com.cinema.booking.repositories;

import com.cinema.booking.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    // Tìm toàn bộ danh sách ghế trong 1 phòng chiếu duy nhất (Lấy Map Ghế)
    List<Seat> findByRoom_RoomId(Integer roomId);
}
