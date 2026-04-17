package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fnb_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FnBLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "FnbItem_ID")
    private FnbItem fnbItem;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private int quantity;
}
