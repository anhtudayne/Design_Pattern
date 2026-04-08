package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "seat_row", nullable = false, length = 5)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", columnDefinition = "ENUM('STANDARD', 'VIP', 'COUPLE') DEFAULT 'STANDARD'")
    private SeatType seatType;

    @Column(name = "price_surcharge", precision = 10, scale = 2)
    private BigDecimal priceSurcharge = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum SeatType {
        STANDARD, VIP, COUPLE
    }
}
