package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_ID")
    private User customer;

    @Column(name = "booking_code", nullable = false, unique = true)
    private String booking_code;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Ticket> TicketList;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<FnBLine> fnBLines;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;
}
