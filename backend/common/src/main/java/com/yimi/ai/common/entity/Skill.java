package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private com.yimi.ai.common.enums.SkillType type;

    @Column(length = 50, nullable = false)
    private String category;

    @Column(columnDefinition = "JSON")
    private String parameters;

    @Column(name = "return_type", length = 100)
    private String returnType;

    @Column(length = 20, nullable = false)
    private String version;

    @Column(name = "creator_id", length = 36, nullable = false)
    private String creatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.yimi.ai.common.enums.SkillStatus status = com.yimi.ai.common.enums.SkillStatus.DRAFT;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeout = 30;

    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}