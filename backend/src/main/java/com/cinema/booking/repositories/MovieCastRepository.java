package com.cinema.booking.repositories;

import com.cinema.booking.entities.MovieCast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieCastRepository extends JpaRepository<MovieCast, Integer> {
    List<MovieCast> findByMovie_MovieId(Integer movieId);

    @Query("SELECT mc FROM MovieCast mc JOIN FETCH mc.castMember WHERE mc.movie.movieId = :movieId")
    List<MovieCast> findByMovieIdWithCastMembers(@Param("movieId") Integer movieId);
}

