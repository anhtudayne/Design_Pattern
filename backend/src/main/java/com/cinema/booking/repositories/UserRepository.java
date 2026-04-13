package com.cinema.booking.repositories;

import com.cinema.booking.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);
}
