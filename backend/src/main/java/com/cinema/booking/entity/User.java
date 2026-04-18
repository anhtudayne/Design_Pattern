package com.cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    private UserAccount userAccount;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    /**
     * Tên vai trò Spring Security (không gồm tiền tố ROLE_).
     */
    public abstract String getSpringSecurityRole();

    public void updateProfile() {
        // Stub
    }
}
