package com.cinema.booking.services.impl;

import com.cinema.booking.services.MovieService;

import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.entities.Movie;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    private MovieDTO mapToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setLanguage(movie.getLanguage());
        dto.setAgeRating(movie.getAgeRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setStatus(movie.getStatus());
        return dto;
    }

    private Movie mapToEntity(MovieDTO dto, Movie movie) {
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setLanguage(dto.getLanguage());
        movie.setAgeRating(dto.getAgeRating());
        movie.setPosterUrl(dto.getPosterUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setStatus(dto.getStatus());
        return movie;
    }

    @Override
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<MovieDTO> getMoviesByStatus(MovieStatus status) {
        return movieRepository.findByStatus(status).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public MovieDTO getMovieById(Integer id) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn phim này!"));
        return mapToDTO(movie);
    }

    @Override
    public MovieDTO createMovie(MovieDTO dto) {
        Movie movie = new Movie();
        mapToEntity(dto, movie);
        return mapToDTO(movieRepository.save(movie));
    }

    @Override
    public MovieDTO updateMovie(Integer id, MovieDTO dto) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không thể Cập nhật: Phim này không tồn tại!"));
        mapToEntity(dto, movie);
        return mapToDTO(movieRepository.save(movie));
    }

    @Override
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }
}
