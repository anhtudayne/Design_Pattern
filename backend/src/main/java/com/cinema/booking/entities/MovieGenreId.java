package com.cinema.booking.entities;

import java.io.Serializable;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreId implements Serializable {
    private Integer movie; // Tên biến phải khớp với tên field trong MovieGenre
    private Integer genre;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieGenreId that = (MovieGenreId) o;
        return Objects.equals(movie, that.movie) &&
               Objects.equals(genre, that.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, genre);
    }
}
