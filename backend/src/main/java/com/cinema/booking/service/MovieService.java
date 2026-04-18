package com.cinema.booking.service;

import com.cinema.booking.dto.MovieDTO;
import com.cinema.booking.entity.Movie.MovieStatus;
import java.util.List;

public interface MovieService {
    List<MovieDTO> getAllMovies();
    List<MovieDTO> getMoviesByStatus(MovieStatus status);
    MovieDTO getMovieById(Integer id);
    MovieDTO createMovie(MovieDTO movieDTO);
    MovieDTO updateMovie(Integer id, MovieDTO movieDTO);
    void deleteMovie(Integer id);
}
