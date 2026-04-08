package com.cinema.booking.repositories;

import com.cinema.booking.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    // Tìm toàn bộ Phòng Chiếu nằm trong 1 Cụm rạp
    List<Room> findByCinema_CinemaId(Integer cinemaId);
}
