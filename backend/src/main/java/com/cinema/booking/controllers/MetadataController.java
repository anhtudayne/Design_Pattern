package com.cinema.booking.controllers;

import com.cinema.booking.entities.Artist;
import com.cinema.booking.entities.CastMember;
import com.cinema.booking.entities.Genre;
import com.cinema.booking.repositories.ArtistRepository;
import com.cinema.booking.repositories.CastMemberRepository;
import com.cinema.booking.repositories.GenreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/metadata")
@Tag(name = "9. Quản lý Metadata (Phim)", description = "Các API CRUD cho Thể loại, Nhân sự điện ảnh và Nghệ sĩ")
public class MetadataController {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private CastMemberRepository castMemberRepository;

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

    // --- CAST MEMBERS ---

    @Operation(summary = "Lấy danh sách nhân sự điện ảnh (CastMember)")
    @GetMapping("/cast-members")
    public List<CastMember> getAllCastMembers() {
        return castMemberRepository.findAll();
    }

    @Operation(summary = "Thêm nhân sự điện ảnh mới (CastMember)")
    @PostMapping("/cast-members")
    public CastMember createCastMember(@RequestBody CastMember castMember) {
        return castMemberRepository.save(castMember);
    }

    @Operation(summary = "Cập nhật nhân sự điện ảnh (CastMember)")
    @PutMapping("/cast-members/{id}")
    public CastMember updateCastMember(@PathVariable Integer id, @RequestBody CastMember details) {
        CastMember castMember = castMemberRepository.findById(id).orElseThrow();
        castMember.setFullName(details.getFullName());
        castMember.setBio(details.getBio());
        return castMemberRepository.save(castMember);
    }

    @Operation(summary = "Xóa nhân sự điện ảnh (CastMember)")
    @DeleteMapping("/cast-members/{id}")
    public void deleteCastMember(@PathVariable Integer id) {
        castMemberRepository.deleteById(id);
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
