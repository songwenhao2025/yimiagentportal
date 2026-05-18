package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private com.yimi.ai.common.enums.DocumentType type;

    @Column(nullable = false)
    @Builder.Default
    private Long size = 0L;

    @Column(name = "uploaded_by", length = 36, nullable = false)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.yimi.ai.common.enums.DocumentStatus status = com.yimi.ai.common.enums.DocumentStatus.UPLOADING;

    @Column(name = "vector_status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.yimi.ai.common.enums.VectorStatus vectorStatus = com.yimi.ai.common.enums.VectorStatus.PENDING;

    @Column(name = "chunk_count", nullable = false)
    @Builder.Default
    private Integer chunkCount = 0;

    @Column(name = "hit_count", nullable = false)
    @Builder.Default
    private Integer hitCount = 0;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}