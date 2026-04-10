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
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name; // STANDARD, VIP, COUPLE

    @Column(name = "price_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSurcharge = BigDecimal.ZERO;
}

