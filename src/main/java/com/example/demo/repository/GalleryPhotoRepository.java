package com.example.demo.repository;

import com.example.demo.entity.GalleryPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, Long> {
    List<GalleryPhoto> findByAlbumId(Long albumId);

    Page<GalleryPhoto> findByAlbumId(Long albumId, Pageable pageable);
}
