package com.example.demo.repository;

import com.example.demo.entity.GalleryAlbum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GalleryAlbumRepository extends JpaRepository<GalleryAlbum, Long> {
    List<GalleryAlbum> findByUserId(Long userId);

    Page<GalleryAlbum> findByUserId(Long userId, Pageable pageable);

    Optional<GalleryAlbum> findByIdAndUserId(Long id, Long userId);
}
