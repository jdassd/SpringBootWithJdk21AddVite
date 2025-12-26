package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.GalleryAlbum;
import com.example.demo.entity.GalleryPhoto;
import com.example.demo.entity.Site;
import com.example.demo.repository.GalleryAlbumRepository;
import com.example.demo.repository.GalleryPhotoRepository;
import com.example.demo.service.SiteService;
import com.example.demo.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/public/{siteKey}/gallery")
@CrossOrigin(origins = "*")
public class PublicGalleryController {
    private final SiteService siteService;
    private final GalleryAlbumRepository albumRepository;
    private final GalleryPhotoRepository photoRepository;

    public PublicGalleryController(SiteService siteService, GalleryAlbumRepository albumRepository,
            GalleryPhotoRepository photoRepository) {
        this.siteService = siteService;
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
    }

    @GetMapping("/albums")
    public PagedResponse<GalleryAlbum> listAlbums(@PathVariable String siteKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Site site = siteService.resolveSite(siteKey);
        Page<GalleryAlbum> albums = albumRepository.findByUserId(site.getOwnerUserId(),
                PaginationUtils.toPageable(page, size, sort, "createdAt"));
        return new PagedResponse<>(albums.getContent(), albums.getNumber(), albums.getSize(), albums.getTotalElements());
    }

    @GetMapping("/albums/{albumId}/photos")
    public List<GalleryPhoto> listPhotos(@PathVariable String siteKey, @PathVariable Long albumId) {
        Site site = siteService.resolveSite(siteKey);
        albumRepository.findByIdAndUserId(albumId, site.getOwnerUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        return photoRepository.findByAlbumId(albumId);
    }
}
