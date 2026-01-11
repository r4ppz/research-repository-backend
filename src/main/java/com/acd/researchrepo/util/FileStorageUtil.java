package com.acd.researchrepo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileStorageUtil {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    private final List<String> allowedContentTypes = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    public String storeFile(MultipartFile file) {
        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename to avoid conflicts
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = System.currentTimeMillis() + "_" +
                    java.util.UUID.randomUUID().toString() +
                    extension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            return "/files/" + uniqueFilename; // Return relative path
        } catch (IOException e) {
            throw new ApiException(ErrorCode.FILE_STORAGE_ERROR, "Failed to store file: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            if (filePath != null) {
                Path path = Paths.get(uploadDir).resolve(getFilenameFromPath(filePath));
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            // Log the error but don't throw exception as it's not critical for the
            // operation
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "File is required");
        }

        // Check file size (20MB max)
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, "File exceeds 20MB limit");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Only PDF and DOCX files are allowed.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    private String getFilenameFromPath(String filePath) {
        if (filePath == null) {
            return null;
        }
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            return filePath.substring(lastSlashIndex + 1);
        }
        return filePath;
    }
}
