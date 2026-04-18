package com.cinema.booking.repository;

import com.cinema.booking.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    @Query("SELECT ua FROM UserAccount ua JOIN FETCH ua.user WHERE ua.email = :email")
    Optional<UserAccount> findByEmailWithUser(@Param("email") String email);

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
