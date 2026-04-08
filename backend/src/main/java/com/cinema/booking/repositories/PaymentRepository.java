package com.cinema.booking.repositories;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBookingAndPaymentMethodAndStatus(Booking booking, String paymentMethod, Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user u WHERE u.userId = :userId ORDER BY p.paymentId DESC")
    List<Payment> findUserPaymentHistory(@Param("userId") Integer userId);
}
