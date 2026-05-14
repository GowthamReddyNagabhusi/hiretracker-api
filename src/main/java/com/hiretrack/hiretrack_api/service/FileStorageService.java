package com.hiretrack.hiretrack_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Handles file storage operations for resume uploads.
 * Files are stored on the local filesystem with UUID-based naming
 * to prevent path traversal attacks and filename collisions.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".doc", ".docx");
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    @Value("${file.upload.dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        // Validate file size programmatically (defense-in-depth beyond Spring config)
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Sanitize filename — only use extension, generate safe UUID name
            String originalName = file.getOriginalFilename();
            String extension = extractExtension(originalName);

            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException("Only PDF, DOC, DOCX files are allowed. Got: " + extension);
            }

            String safeFileName = UUID.randomUUID() + extension.toLowerCase();
            Path targetLocation = uploadPath.resolve(safeFileName).normalize();

            // Prevent path traversal: ensure target is within upload directory
            if (!targetLocation.startsWith(uploadPath)) {
                throw new SecurityException("Path traversal attempt detected");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {} (original: {})", safeFileName, originalName);

            return safeFileName;
        } catch (IOException ex) {
            log.error("Failed to store file: {}", ex.getMessage());
            throw new RuntimeException("Could not store file. Please try again.");
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(fileName).normalize();

            // Prevent path traversal on delete
            if (!filePath.startsWith(uploadPath)) {
                log.warn("Path traversal attempt on delete: {}", fileName);
                return;
            }

            if (Files.deleteIfExists(filePath)) {
                log.info("Deleted file: {}", fileName);
            }
        } catch (IOException ex) {
            log.error("Failed to delete file {}: {}", fileName, ex.getMessage());
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
