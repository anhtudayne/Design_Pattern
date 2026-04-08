package com.cinema.booking.services;

import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.entities.Movie.MovieStatus;
import java.util.List;

public interface MovieService {
    List<MovieDTO> getAllMovies();
    List<MovieDTO> getMoviesByStatus(MovieStatus status);
    MovieDTO getMovieById(Integer id);
    MovieDTO createMovie(MovieDTO movieDTO);
    MovieDTO updateMovie(Integer id, MovieDTO movieDTO);
    void deleteMovie(Integer id);
}
