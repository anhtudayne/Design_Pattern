package com.cinema.booking.repositories;

import com.cinema.booking.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    @Modifying
    @Query(value = "UPDATE customers SET total_spending = COALESCE(total_spending, 0) + :amount WHERE user_id = :userId", nativeQuery = true)
    int increaseTotalSpending(@Param("userId") Integer userId, @Param("amount") BigDecimal amount);
}
