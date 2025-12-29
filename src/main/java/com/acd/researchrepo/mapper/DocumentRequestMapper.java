package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.model.UserDocumentRequestDto;
import com.acd.researchrepo.model.DocumentRequest;

import org.springframework.stereotype.Component;

@Component
public class DocumentRequestMapper {
    private final ResearchPaperMapper researchPaperMapper;

    public DocumentRequestMapper(ResearchPaperMapper researchPaperMapper) {
        this.researchPaperMapper = researchPaperMapper;
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
}
