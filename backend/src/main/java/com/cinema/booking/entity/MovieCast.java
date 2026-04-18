package com.cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_casts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieCast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cast_member_id")
    private CastMember castMember;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "role_name")
    private String role_name;

    @Column(name = "role_type")
    private String role_type;
}
