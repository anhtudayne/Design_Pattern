package com.cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity khách hàng — mở rộng {@link User} với thông tin hạng thành viên.
 *
 * <p>{@code membershipDiscountPercent}: % giảm giá theo hạng thành viên (nullable = chưa có hạng).
 * Ví dụ: 5.00 → giảm 5% trên phần tiền còn lại sau khi áp mã khuyến mãi.
 */
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User {

    /**
     * Phần trăm giảm giá theo hạng thành viên.
     * Null = chưa có hạng (không được giảm).
     * Ví dụ: 5 = Silver (5%), 10 = Gold (10%), 15 = Platinum (15%).
     */
    @Column(name = "membership_discount_percent", precision = 5, scale = 2)
    private BigDecimal membershipDiscountPercent;

    @Override
    public String getSpringSecurityRole() {
        return "USER";
    }

    public void bookTicket() {
        // Stub
    }

    public void writeReview() {
        // Stub
    }
}

