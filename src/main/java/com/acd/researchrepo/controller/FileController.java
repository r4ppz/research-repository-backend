package com.acd.researchrepo.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;

import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final ResearchPaperService researchPaperService;

    public FileController(ResearchPaperService researchPaperService) {
        this.researchPaperService = researchPaperService;
    }

    @GetMapping("/{paperId}")
    @Operation(summary = "Download or view a research paper file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer paperId,
            @RequestParam(defaultValue = "false") boolean view,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        Path filePath = researchPaperService.downloadPaper(paperId, userPrincipal);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = determineContentType(filePath);
            String disposition = view ? "inline" : "attachment";
            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ApiException(ErrorCode.FILE_STORAGE_ERROR, "Could not read file");
        }
    }

    private String determineContentType(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        if (filename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }
}
