package com.cinema.booking.repositories;

import com.cinema.booking.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByBooking_Showtime_ShowtimeId(Integer showtimeId);
    List<Ticket> findByBooking_BookingId(Integer bookingId);
    List<Ticket> findByBooking_User_UserId(Integer userId);
    boolean existsByBooking_Showtime_ShowtimeIdAndSeat_SeatId(Integer showtimeId, Integer seatId);
}
