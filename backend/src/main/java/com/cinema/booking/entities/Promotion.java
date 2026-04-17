package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_type")
    private String discount_type;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discount_value;

    @Column(name = "valid_to")
    private LocalDateTime valid_to;
}
