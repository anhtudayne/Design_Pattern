package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "movie_ID")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "showtime_ID")
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "seat_ID")
    private Seat seat;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unit_price;

    @Column(name = "hold_expires_at")
    private LocalDateTime hold_expires_at;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
