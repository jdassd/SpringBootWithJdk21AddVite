package com.example.demo.controller;

import com.example.demo.entity.GalleryAlbum;
import com.example.demo.entity.GalleryPhoto;
import com.example.demo.repository.GalleryAlbumRepository;
import com.example.demo.repository.GalleryPhotoRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/gallery")
@CrossOrigin(origins = "*")
public class GalleryController {
    private final GalleryAlbumRepository albumRepository;
    private final GalleryPhotoRepository photoRepository;

    public GalleryController(GalleryAlbumRepository albumRepository, GalleryPhotoRepository photoRepository) {
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
    }

    @GetMapping("/albums")
    public List<GalleryAlbum> listAlbums(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return albumRepository.findByUserId(userId);
    }

    @GetMapping("/public/albums")
    public List<GalleryAlbum> listPublicAlbums(HttpServletRequest request) {
        return listAlbums(request);
    }

    @PostMapping("/albums")
    public GalleryAlbum createAlbum(HttpServletRequest request, @Valid @RequestBody AlbumRequest albumRequest) {
        Long userId = (Long) request.getAttribute("userId");
        GalleryAlbum album = new GalleryAlbum();
        album.setUserId(userId);
        album.setTitle(albumRequest.title());
        album.setDescription(albumRequest.description());
        album.setCoverUrl(albumRequest.coverUrl());
        album.setCreatedAt(Instant.now());
        albumRepository.save(album);
        return album;
    }

    @PutMapping("/albums/{id}")
    public GalleryAlbum updateAlbum(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        GalleryAlbum album = albumRepository.findById(id).orElseThrow();
        album.setTitle(request.title());
        album.setDescription(request.description());
        album.setCoverUrl(request.coverUrl());
        albumRepository.save(album);
        return album;
    }

    @DeleteMapping("/albums/{id}")
    public void deleteAlbum(@PathVariable Long id) {
        albumRepository.deleteById(id);
    }

    @GetMapping("/albums/{albumId}/photos")
    public List<GalleryPhoto> listPhotos(@PathVariable Long albumId) {
        return photoRepository.findByAlbumId(albumId);
    }

    @GetMapping("/public/albums/{albumId}/photos")
    public List<GalleryPhoto> listPublicPhotos(@PathVariable Long albumId) {
        return listPhotos(albumId);
    }

    @PostMapping("/albums/{albumId}/photos")
    public GalleryPhoto createPhoto(@PathVariable Long albumId, @Valid @RequestBody PhotoRequest request) {
        GalleryPhoto photo = new GalleryPhoto();
        photo.setAlbumId(albumId);
        photo.setTitle(request.title());
        photo.setImageUrl(request.imageUrl());
        photo.setTakenAt(request.takenAt());
        photo.setCreatedAt(Instant.now());
        photoRepository.save(photo);
        return photo;
    }

    @DeleteMapping("/photos/{id}")
    public void deletePhoto(@PathVariable Long id) {
        photoRepository.deleteById(id);
    }

    public record AlbumRequest(
            @NotBlank String title,
            String description,
            String coverUrl) {
    }

    public record PhotoRequest(
            @NotBlank String title,
            @NotBlank String imageUrl,
            Instant takenAt) {
    }
}
