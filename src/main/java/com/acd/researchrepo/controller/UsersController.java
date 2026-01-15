package com.acd.researchrepo.controller;

import java.util.List;

import com.acd.researchrepo.dto.external.model.UserDocumentRequestDto;
import com.acd.researchrepo.dto.external.model.UserDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DocumentRequestService;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

        UserDto userDto = userMapper.toDto(principal.getUser());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/me/requests")
    public ResponseEntity<PaginatedResponse<UserDocumentRequestDto>> getUserRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "status", required = false) List<RequestStatus> statuses,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        log.debug("api/users/me/requests endpoint hit");

        if (!RoleBasedAccess.isUserStudentOrTeacher(principal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        return ResponseEntity.ok(documentRequestService.getUserDocumentRequests(
                principal, statuses, search, sortBy, sortOrder, page, size));
    }
}
