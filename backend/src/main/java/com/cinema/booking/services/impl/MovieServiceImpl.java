package com.cinema.booking.services.impl;

import com.cinema.booking.services.MovieService;

import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.dtos.MovieDTO.MovieCastDTO;
import com.cinema.booking.entities.CastMember;
import com.cinema.booking.entities.Movie;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.entities.MovieCast;
import com.cinema.booking.repositories.CastMemberRepository;
import com.cinema.booking.repositories.MovieCastRepository;
import com.cinema.booking.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("movieServiceImpl")
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CastMemberRepository castMemberRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

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

        List<MovieCast> casts = movieCastRepository.findByMovie_MovieId(movie.getMovieId());
        dto.setCasts(casts.stream().map(mc -> {
            MovieCastDTO c = new MovieCastDTO();
            c.setId(mc.getId());
            if (mc.getCastMember() != null) {
                c.setCastMemberId(mc.getCastMember().getId());
                c.setCastMemberName(mc.getCastMember().getFullName());
                c.setCastMemberBio(mc.getCastMember().getBio());
                c.setCastMemberImageUrl(mc.getCastMember().getImageUrl());
            }
            c.setRoleName(mc.getRoleName());
            c.setRoleType(mc.getRoleType());
            return c;
        }).toList());

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

    private void upsertMovieCasts(Movie movie, List<MovieCastDTO> castDTOs) {
        // Replace-all strategy for simplicity and consistency.
        List<MovieCast> existing = movieCastRepository.findByMovie_MovieId(movie.getMovieId());
        if (!existing.isEmpty()) {
            movieCastRepository.deleteAll(existing);
        }

        if (castDTOs == null || castDTOs.isEmpty()) {
            return;
        }

        List<MovieCast> toSave = castDTOs.stream().map(cdto -> {
            if (cdto.getCastMemberId() == null) {
                throw new RuntimeException("castMemberId là bắt buộc cho mỗi phần tử casts");
            }
            if (cdto.getRoleType() == null) {
                throw new RuntimeException("roleType là bắt buộc cho mỗi phần tử casts");
            }

            CastMember member = castMemberRepository.findById(cdto.getCastMemberId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy CastMember ID: " + cdto.getCastMemberId()));

            return MovieCast.builder()
                    .movie(movie)
                    .castMember(member)
                    .roleName(cdto.getRoleName())
                    .roleType(cdto.getRoleType())
                    .build();
        }).toList();

        movieCastRepository.saveAll(toSave);
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
        movie = movieRepository.save(movie);
        upsertMovieCasts(movie, dto.getCasts());
        return mapToDTO(movie);
    }

    @Override
    public MovieDTO updateMovie(Integer id, MovieDTO dto) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không thể Cập nhật: Phim này không tồn tại!"));
        mapToEntity(dto, movie);
        movie = movieRepository.save(movie);
        upsertMovieCasts(movie, dto.getCasts());
        return mapToDTO(movie);
    }

    @Override
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }
}
