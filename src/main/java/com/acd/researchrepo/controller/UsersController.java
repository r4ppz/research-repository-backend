package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.external.model.UserDto;
import com.acd.researchrepo.dto.external.requests.UserDocumentRequestsResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DocumentRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserMapper userMapper;
    private final DocumentRequestService documentRequestService;

    public UsersController(UserMapper userMapper, DocumentRequestService documentRequestService) {
        this.userMapper = userMapper;
        this.documentRequestService = documentRequestService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserPrincipal principal) {
        log.debug("api/users/me endpoint hit");

        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        UserDto userDto = userMapper.toDto(principal.getUser());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/me/requests")
    public ResponseEntity<UserDocumentRequestsResponse> getUserRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        log.debug("api/users/me/requests endpoint hit");

        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        return ResponseEntity.ok(documentRequestService.getUserDocumentRequests(principal));
    }
}
