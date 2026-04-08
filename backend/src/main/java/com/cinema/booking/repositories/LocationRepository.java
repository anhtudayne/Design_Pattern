package com.cinema.booking.repositories;

import com.cinema.booking.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    // Kế thừa JpaRepository tự động có sẵn các hàm findAll, findById, save, deleteById...
}
