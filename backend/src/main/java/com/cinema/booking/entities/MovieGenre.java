package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Bảng nối EXPLICIT giữa Movie và Genre (junction table movie_genres).
 * Class diagram: Movie "*" -- "*" Genre
 */
@Entity
@Table(name = "movie_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MovieGenreId.class)
public class MovieGenre {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}
