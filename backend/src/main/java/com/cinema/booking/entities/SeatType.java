package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seat_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_ID")
    private Integer seat_ID;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_surcharge", precision = 10, scale = 2)
    private BigDecimal price_surcharge;
}
