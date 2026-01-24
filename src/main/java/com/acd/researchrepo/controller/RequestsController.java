package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.external.requests.CreateRequestRequest;
import com.acd.researchrepo.dto.external.requests.CreateRequestResponse;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DocumentRequestService;

import org.springframework.http.HttpStatus;
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

    private final DocumentRequestService requestService;

    public RequestsController(DocumentRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ResponseEntity<CreateRequestResponse> createRequest(
            @Valid @RequestBody CreateRequestRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        log.debug("api/requests endpoint hit");
        CreateRequestResponse response = requestService.createRequest(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        log.debug("api/requests/{} endpoint hit", requestId);
        requestService.deleteRequest(requestId, user);
        return ResponseEntity.noContent().build();
    }
}
