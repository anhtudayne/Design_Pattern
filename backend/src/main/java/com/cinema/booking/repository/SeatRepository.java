package com.cinema.booking.repository;

import com.cinema.booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    // Tìm toàn bộ danh sách ghế trong 1 phòng chiếu duy nhất (Lấy Map Ghế)
    List<Seat> findByRoom_RoomId(Integer roomId);

    long countByRoom_RoomId(Integer roomId);
}
