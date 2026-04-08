package com.cinema.booking.repositories;

import com.cinema.booking.entities.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {
    List<MovieGenre> findByMovieMovieId(Integer movieId);
    void deleteByMovieMovieId(Integer movieId);
    void deleteByMovieMovieIdAndGenreGenreId(Integer movieId, Integer genreId);
}
