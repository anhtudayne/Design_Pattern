package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false, unique = true)
    private Promotion promotion;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
