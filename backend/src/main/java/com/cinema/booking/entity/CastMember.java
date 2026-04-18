package com.cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "cast_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cast_member_id")
    private Integer castMemberId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "image_url")
    private String imageUrl;
}

