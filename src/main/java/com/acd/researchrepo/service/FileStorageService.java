package com.acd.researchrepo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.acd.researchrepo.environment.AppProperties;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileStorageService {

    private final Path rootLocation;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    public FileStorageService(AppProperties appProperties) {
        this.rootLocation = Paths.get(appProperties.getStorage().getUploadDir());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
            throw new ApiException(ErrorCode.FILE_STORAGE_ERROR, "Could not initialize storage location");
        }
    }

    /**
     * Saves a file to the storage location.
     *
     * @param file    The file to save
     * @param subPath The relative subpath (e.g., "2023/dept_cs/paper_1.pdf")
     * @return The path where the file was saved
     */
    public String saveFile(MultipartFile file, String subPath) {
        validateFile(file);

        try {
            Path destinationFile = rootLocation.resolve(Paths.get(subPath)).normalize().toAbsolutePath();

            if (!destinationFile.getParent().startsWith(rootLocation.toAbsolutePath())) {
                // Security check: ensure the file is not being saved outside the root location
                throw new ApiException(ErrorCode.INVALID_REQUEST, "Cannot store file outside current directory");
            }

            Files.createDirectories(destinationFile.getParent());
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return subPath;
        } catch (IOException e) {
            log.error("Failed to store file: {}", subPath, e);
            throw new ApiException(ErrorCode.FILE_STORAGE_ERROR, "Failed to store file");
        }
    }

    /**
     * Deletes a file from the storage location.
     *
     * @param subPath The relative subpath of the file to delete
     */
    public void deleteFile(String subPath) {
        try {
            Path file = rootLocation.resolve(subPath);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Could not delete file: {}", subPath, e);
            throw new ApiException(ErrorCode.FILE_STORAGE_ERROR, "Could not delete file");
        }
    }

    /**
     * Loads a file as a Path.
     *
     * @param subPath The relative subpath of the file to load
     * @return The Path of the file
     */
    public Path loadFile(String subPath) {
        Path file = rootLocation.resolve(subPath).normalize();
        if (!Files.exists(file)) {
            throw new ApiException(ErrorCode.FILE_NOT_FOUND, "File not found");
        }
        return file;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, "File exceeds 20MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Only PDF and DOCX files are allowed");
        }
    }
}
