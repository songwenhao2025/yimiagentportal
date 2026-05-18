package com.yimi.ai.knowledge.service;

import com.yimi.ai.knowledge.dto.DocumentCreateRequest;
import com.yimi.ai.knowledge.dto.DocumentResponse;
import com.yimi.ai.knowledge.repository.KnowledgeDocumentRepository;
import com.yimi.ai.common.entity.KnowledgeDocument;
import com.yimi.ai.common.enums.DocumentStatus;
import com.yimi.ai.common.enums.DocumentType;
import com.yimi.ai.common.enums.VectorStatus;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocumentRepository documentRepository;

    public PageResponse<DocumentResponse> list(String category, String status, String type, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<KnowledgeDocument> documentPage;

        if (keyword != null && !keyword.isEmpty()) {
            documentPage = documentRepository.findByTitleContaining(keyword, pageable);
        } else if (category != null) {
            documentPage = documentRepository.findByCategory(category, pageable);
        } else if (status != null) {
            documentPage = documentRepository.findByStatus(DocumentStatus.fromCode(status), pageable);
        } else if (type != null) {
            documentPage = documentRepository.findByType(DocumentType.fromCode(type), pageable);
        } else {
            documentPage = documentRepository.findAll(pageable);
        }

        List<DocumentResponse> responses = documentPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, documentPage.getTotalElements(), page, size);
    }

    public DocumentResponse get(String id) {
        KnowledgeDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文档不存在"));
        return convertToResponse(document);
    }

    public DocumentResponse create(DocumentCreateRequest request) {
        KnowledgeDocument document = KnowledgeDocument.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .type(DocumentType.fromCode(request.getType()))
                .size(request.getSize() != null ? request.getSize() : 0L)
                .uploadedBy(request.getUploadedBy())
                .status(DocumentStatus.UPLOADING)
                .vectorStatus(VectorStatus.PENDING)
                .chunkCount(0)
                .hitCount(0)
                .category(request.getCategory())
                .filePath(request.getFilePath())
                .fileUrl(request.getFileUrl())
                .build();

        KnowledgeDocument saved = documentRepository.save(document);
        return convertToResponse(saved);
    }

    public DocumentResponse update(String id, DocumentCreateRequest request) {
        KnowledgeDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文档不存在"));

        document.setTitle(request.getTitle());
        document.setType(DocumentType.fromCode(request.getType()));
        document.setSize(request.getSize() != null ? request.getSize() : document.getSize());
        document.setCategory(request.getCategory());
        document.setFilePath(request.getFilePath());
        document.setFileUrl(request.getFileUrl());

        KnowledgeDocument saved = documentRepository.save(document);
        return convertToResponse(saved);
    }

    public void delete(String id) {
        if (!documentRepository.existsById(id)) {
            throw new BusinessException(404, "文档不存在");
        }
        documentRepository.deleteById(id);
    }

    public List<DocumentResponse> search(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<KnowledgeDocument> documents = documentRepository.findByTitleContaining(query, pageable);
        return documents.getContent().stream()
                .map(this::convertToResponse)
                .toList();
    }

    private DocumentResponse convertToResponse(KnowledgeDocument document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .type(document.getType().getCode())
                .size(document.getSize())
                .uploadedBy(document.getUploadedBy())
                .uploadedAt(document.getUploadedAt().toString())
                .status(document.getStatus().getCode())
                .vectorStatus(document.getVectorStatus().getCode())
                .chunkCount(document.getChunkCount())
                .hitCount(document.getHitCount())
                .category(document.getCategory())
                .filePath(document.getFilePath())
                .fileUrl(document.getFileUrl())
                .build();
    }
}