package com.cinema.booking.repository;

import com.cinema.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByShowtime_ShowtimeId(Integer showtimeId);
    List<Ticket> findByBooking_BookingId(Integer bookingId);
    List<Ticket> findByBooking_User_UserId(Integer userId);
    boolean existsByShowtime_ShowtimeIdAndSeat_SeatId(Integer showtimeId, Integer seatId);

    long countBySeat_SeatId(Integer seatId);

    long countByShowtime_ShowtimeId(Integer showtimeId);
}
