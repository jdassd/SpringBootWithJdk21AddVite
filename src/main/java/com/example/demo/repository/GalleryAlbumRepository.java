package com.example.demo.repository;

import com.example.demo.entity.GalleryAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryAlbumRepository extends JpaRepository<GalleryAlbum, Long> {
}
