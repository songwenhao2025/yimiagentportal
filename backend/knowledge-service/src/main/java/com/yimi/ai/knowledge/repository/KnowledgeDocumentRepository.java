package com.yimi.ai.knowledge.repository;

import com.yimi.ai.common.entity.KnowledgeDocument;
import com.yimi.ai.common.enums.DocumentStatus;
import com.yimi.ai.common.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, String> {

    Page<KnowledgeDocument> findByCategory(String category, Pageable pageable);

    Page<KnowledgeDocument> findByStatus(DocumentStatus status, Pageable pageable);

    Page<KnowledgeDocument> findByType(DocumentType type, Pageable pageable);

    Page<KnowledgeDocument> findByTitleContaining(String title, Pageable pageable);

    List<KnowledgeDocument> findByUploadedBy(String uploadedBy);

    List<KnowledgeDocument> findByStatus(DocumentStatus status);
}