package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.requests.UserDocumentRequestDto;
import com.acd.researchrepo.model.DocumentRequest;

import org.springframework.stereotype.Component;

@Component
public class DocumentRequestMapper {

    public UserDocumentRequestDto toDto(DocumentRequest request) {
        if (request == null) {
            return null;
        }

        return UserDocumentRequestDto.builder()
                .requestId(request.getRequestId())
                .paperId(request.getPaper().getPaperId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt() != null ? request.getCreatedAt() : request.getRequestDate())
                .updatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : request.getRequestDate())
                .build();
    }
}
