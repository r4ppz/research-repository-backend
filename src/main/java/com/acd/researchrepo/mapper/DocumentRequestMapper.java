package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.model.UserDocumentRequestDto;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.dto.external.requests.AdminRequestResponse;
import com.acd.researchrepo.model.DocumentRequest;

import org.springframework.stereotype.Component;

@Component
public class DocumentRequestMapper {
    private final ResearchPaperMapper researchPaperMapper;
    private final UserMapper userMapper;

    public DocumentRequestMapper(ResearchPaperMapper researchPaperMapper, UserMapper userMapper) {
        this.researchPaperMapper = researchPaperMapper;
        this.userMapper = userMapper;
    }

    public UserDocumentRequestDto toDto(DocumentRequest request) {
        if (request == null) {
            return null;
        }

        ResearchPaperDto paperDto = researchPaperMapper.toDto(request.getPaper());

        return UserDocumentRequestDto.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : request.getRequestDate())
                .updatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getRequestDate())
                .paper(paperDto)
                .build();
    }

    public AdminRequestResponse toAdminDto(DocumentRequest request) {
        if (request == null)
            return null;

        return AdminRequestResponse.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : request.getRequestDate())
                .updatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getRequestDate())
                .user(userMapper.toDto(request.getUser()))
                .paper(researchPaperMapper.toDto(request.getPaper()))
                .build();
    }

    public PaperUserRequestResponse toPaperUserRequestResponse(DocumentRequest request) {
        if (request == null)
            return null;
        return PaperUserRequestResponse.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
