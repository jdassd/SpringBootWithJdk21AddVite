package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaStorageService {
    private final Path rootPath;

    public MediaStorageService(@Value("${app.media.storage-path:uploads}") String storagePath) throws IOException {
        this.rootPath = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(this.rootPath);
    }

    public StoredFile store(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = rootPath.resolve(storedName).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFile(storedName, target.toString());
    }

    public Resource loadAsResource(String storagePath) {
        try {
            Path path = Paths.get(storagePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException ignored) {
            // fall through
        }
        return null;
    }

    public record StoredFile(String fileName, String storagePath) {
    }
}
