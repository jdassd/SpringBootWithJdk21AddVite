package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.GalleryAlbum;
import com.example.demo.entity.GalleryPhoto;
import com.example.demo.repository.GalleryAlbumRepository;
import com.example.demo.repository.GalleryPhotoRepository;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public PagedResponse<GalleryAlbum> listAlbums(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<GalleryAlbum> albums = albumRepository.findByUserId(userId, PaginationUtils.toPageable(page, size, sort, "createdAt"));
        return new PagedResponse<>(albums.getContent(), albums.getNumber(), albums.getSize(), albums.getTotalElements());
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
    public GalleryAlbum updateAlbum(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody AlbumRequest update) {
        Long userId = (Long) request.getAttribute("userId");
        GalleryAlbum album = albumRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        album.setTitle(update.title());
        album.setDescription(update.description());
        album.setCoverUrl(update.coverUrl());
        albumRepository.save(album);
        return album;
    }

    @DeleteMapping("/albums/{id}")
    public void deleteAlbum(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        GalleryAlbum album = albumRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        albumRepository.deleteById(album.getId());
    }

    @GetMapping("/albums/{albumId}/photos")
    public List<GalleryPhoto> listPhotos(HttpServletRequest request, @PathVariable Long albumId) {
        Long userId = (Long) request.getAttribute("userId");
        albumRepository.findByIdAndUserId(albumId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        return photoRepository.findByAlbumId(albumId);
    }

    @PostMapping("/albums/{albumId}/photos")
    public GalleryPhoto createPhoto(HttpServletRequest request, @PathVariable Long albumId,
            @Valid @RequestBody PhotoRequest photoRequest) {
        Long userId = (Long) request.getAttribute("userId");
        albumRepository.findByIdAndUserId(albumId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        GalleryPhoto photo = new GalleryPhoto();
        photo.setAlbumId(albumId);
        photo.setTitle(photoRequest.title());
        photo.setImageUrl(photoRequest.imageUrl());
        photo.setTakenAt(photoRequest.takenAt());
        photo.setCreatedAt(Instant.now());
        photoRepository.save(photo);
        return photo;
    }

    @DeleteMapping("/photos/{id}")
    public void deletePhoto(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        GalleryPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
        albumRepository.findByIdAndUserId(photo.getAlbumId(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        photoRepository.deleteById(photo.getId());
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
