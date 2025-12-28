package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.external.requests.CreateRequestRequestDto;
import com.acd.researchrepo.dto.external.requests.CreateRequestResponseDto;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DocumentRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/requests")
public class RequestsController {

    private final DocumentRequestService documentRequestService;

    public RequestsController(DocumentRequestService documentRequestService) {
        this.documentRequestService = documentRequestService;
    }

    @PostMapping
    public ResponseEntity<CreateRequestResponseDto> createRequest(
            @Valid @RequestBody CreateRequestRequestDto requestDto,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        log.debug("api/requests endpoint hit!!");

        CreateRequestResponseDto response = documentRequestService.createRequest(requestDto, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        log.debug("api/requests/{} endpoint hit!!", requestId);

        documentRequestService.deleteRequest(requestId, principal);
        return ResponseEntity.noContent().build();
    }
}
