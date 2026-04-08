package com.cinema.booking.repositories;

import com.cinema.booking.entities.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Integer> {
    // Mở rộng thêm hàm tìm Cụm Rạp theo Mã Tỉnh/Thành phố
    List<Cinema> findByLocation_LocationId(Integer locationId);
}
