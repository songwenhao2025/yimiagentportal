package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_id", length = 36, nullable = false)
    private String creatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.yimi.ai.common.enums.WorkflowStatus status = com.yimi.ai.common.enums.WorkflowStatus.DRAFT;

    @Column(name = "trigger_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private com.yimi.ai.common.enums.TriggerType triggerType;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "execution_count", nullable = false)
    @Builder.Default
    private Integer executionCount = 0;

    @Column(name = "success_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private Double successRate = 0.0;

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