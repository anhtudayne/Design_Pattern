package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String fullname;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    private UserAccount userAccount;

    /**
     * Tên vai trò Spring Security (không gồm tiền tố ROLE_).
     * Mỗi subclass tự khai báo role của mình — tuân thủ OCP.
     */
    public abstract String getSpringSecurityRole();
}
