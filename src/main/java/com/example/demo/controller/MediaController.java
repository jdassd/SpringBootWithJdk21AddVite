package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.MediaFile;
import com.example.demo.repository.MediaFileRepository;
import com.example.demo.service.MediaStorageService;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "*")
public class MediaController {
    private final MediaFileRepository mediaRepository;
    private final MediaStorageService storageService;

    public MediaController(MediaFileRepository mediaRepository, MediaStorageService storageService) {
        this.mediaRepository = mediaRepository;
        this.storageService = storageService;
    }

    @GetMapping
    public PagedResponse<MediaFile> list(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<MediaFile> files = mediaRepository.findByUserId(userId,
                PaginationUtils.toPageable(page, size, sort, "createdAt"));
        return new PagedResponse<>(files.getContent(), files.getNumber(), files.getSize(), files.getTotalElements());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFile upload(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are supported");
        }
        Long userId = (Long) request.getAttribute("userId");
        try {
            MediaStorageService.StoredFile storedFile = storageService.store(file);
            MediaFile mediaFile = new MediaFile();
            mediaFile.setUserId(userId);
            mediaFile.setFileName(file.getOriginalFilename());
            mediaFile.setContentType(file.getContentType());
            mediaFile.setSize(file.getSize());
            mediaFile.setStoragePath(storedFile.storagePath());
            mediaFile.setCreatedAt(Instant.now());
            mediaFile = mediaRepository.save(mediaFile);
            String url = "/api/media/files/" + mediaFile.getId();
            mediaFile.setUrl(url);
            mediaFile.setThumbnailUrl(url);
            return mediaRepository.save(mediaFile);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> serveFile(@PathVariable Long id) {
        MediaFile mediaFile = mediaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        Resource resource = storageService.loadAsResource(mediaFile.getStoragePath());
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        MediaType contentType = mediaFile.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(mediaFile.getContentType());
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + mediaFile.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        MediaFile mediaFile = mediaRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
        mediaRepository.deleteById(mediaFile.getId());
        if (mediaFile.getStoragePath() != null) {
            try {
                Files.deleteIfExists(Path.of(mediaFile.getStoragePath()));
            } catch (IOException ignored) {
                // Best effort cleanup.
            }
        }
    }
}
