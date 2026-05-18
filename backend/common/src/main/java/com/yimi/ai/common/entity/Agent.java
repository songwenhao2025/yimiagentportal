package com.yimi.ai.common.entity;

import com.yimi.ai.common.enums.AgentStatus;
import com.yimi.ai.common.enums.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "success_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private Double successRate = 0.0;

    @Column(name = "avg_time", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private Double avgTime = 0.0;

    @Column(name = "daily_calls", nullable = false)
    @Builder.Default
    private Integer dailyCalls = 0;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "creator_id", length = 36, nullable = false)
    private String creatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AgentStatus status = AgentStatus.PENDING;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "rating", precision = 2, scale = 1)
    private Double rating;

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