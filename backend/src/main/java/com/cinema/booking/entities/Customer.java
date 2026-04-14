package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private MembershipTier tier;

    @Column(name = "total_spending", precision = 15, scale = 2)
    private BigDecimal totalSpending = BigDecimal.ZERO;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Override
    public String getSpringSecurityRole() {
        return "USER";
    }
}
