package com.cinema.booking.controllers;

import com.cinema.booking.entities.Actor;
import com.cinema.booking.entities.Artist;
import com.cinema.booking.entities.Director;
import com.cinema.booking.entities.Genre;
import com.cinema.booking.repositories.ActorRepository;
import com.cinema.booking.repositories.ArtistRepository;
import com.cinema.booking.repositories.DirectorRepository;
import com.cinema.booking.repositories.GenreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/metadata")
@Tag(name = "9. Quản lý Metadata (Phim)", description = "Các API CRUD cho Thể loại, Diễn viên, Đạo diễn và Nghệ sĩ")
public class MetadataController {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private ArtistRepository artistRepository;

    // --- GENRES ---

    @Operation(summary = "Lấy danh sách thể loại")
    @GetMapping("/genres")
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @Operation(summary = "Thêm thể loại mới")
    @PostMapping("/genres")
    public Genre createGenre(@RequestBody Genre genre) {
        return genreRepository.save(genre);
    }

    @Operation(summary = "Cập nhật thể loại")
    @PutMapping("/genres/{id}")
    public Genre updateGenre(@PathVariable Integer id, @RequestBody Genre details) {
        Genre genre = genreRepository.findById(id).orElseThrow();
        genre.setName(details.getName());
        return genreRepository.save(genre);
    }

    @Operation(summary = "Xóa thể loại")
    @DeleteMapping("/genres/{id}")
    public void deleteGenre(@PathVariable Integer id) {
        genreRepository.deleteById(id);
    }

    // --- ACTORS ---

    @Operation(summary = "Lấy danh sách diễn viên")
    @GetMapping("/actors")
    public List<Actor> getAllActors() {
        return actorRepository.findAll();
    }

    @Operation(summary = "Thêm diễn viên mới")
    @PostMapping("/actors")
    public Actor createActor(@RequestBody Actor actor) {
        return actorRepository.save(actor);
    }

    @Operation(summary = "Cập nhật diễn viên")
    @PutMapping("/actors/{id}")
    public Actor updateActor(@PathVariable Integer id, @RequestBody Actor details) {
        Actor actor = actorRepository.findById(id).orElseThrow();
        actor.setFullName(details.getFullName());
        actor.setBio(details.getBio());
        actor.setBirthDate(details.getBirthDate());
        actor.setNationality(details.getNationality());
        actor.setImageUrl(details.getImageUrl());
        return actorRepository.save(actor);
    }

    @Operation(summary = "Xóa diễn viên")
    @DeleteMapping("/actors/{id}")
    public void deleteActor(@PathVariable Integer id) {
        actorRepository.deleteById(id);
    }

    // --- DIRECTORS ---

    @Operation(summary = "Lấy danh sách đạo diễn")
    @GetMapping("/directors")
    public List<Director> getAllDirectors() {
        return directorRepository.findAll();
    }

    @Operation(summary = "Thêm đạo diễn mới")
    @PostMapping("/directors")
    public Director createDirector(@RequestBody Director director) {
        return directorRepository.save(director);
    }

    @Operation(summary = "Cập nhật đạo diễn")
    @PutMapping("/directors/{id}")
    public Director updateDirector(@PathVariable Integer id, @RequestBody Director details) {
        Director director = directorRepository.findById(id).orElseThrow();
        director.setFullName(details.getFullName());
        director.setBio(details.getBio());
        director.setBirthDate(details.getBirthDate());
        director.setNationality(details.getNationality());
        director.setImageUrl(details.getImageUrl());
        return directorRepository.save(director);
    }

    @Operation(summary = "Xóa đạo diễn")
    @DeleteMapping("/directors/{id}")
    public void deleteDirector(@PathVariable Integer id) {
        directorRepository.deleteById(id);
    }

    // --- ARTISTS ---

    @Operation(summary = "Lấy danh sách nghệ sĩ")
    @GetMapping("/artists")
    public List<Artist> getAllArtists() {
        return artistRepository.findAll();
    }

    @Operation(summary = "Thêm nghệ sĩ mới")
    @PostMapping("/artists")
    public Artist createArtist(@RequestBody Artist artist) {
        return artistRepository.save(artist);
    }

    @Operation(summary = "Cập nhật nghệ sĩ")
    @PutMapping("/artists/{id}")
    public Artist updateArtist(@PathVariable Integer id, @RequestBody Artist details) {
        Artist artist = artistRepository.findById(id).orElseThrow();
        artist.setFullName(details.getFullName());
        artist.setBio(details.getBio());
        artist.setBirthDate(details.getBirthDate());
        artist.setNationality(details.getNationality());
        artist.setImageUrl(details.getImageUrl());
        return artistRepository.save(artist);
    }

    @Operation(summary = "Xóa nghệ sĩ")
    @DeleteMapping("/artists/{id}")
    public void deleteArtist(@PathVariable Integer id) {
        artistRepository.deleteById(id);
    }
}
