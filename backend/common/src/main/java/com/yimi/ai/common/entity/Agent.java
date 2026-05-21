package com.yimi.ai.common.entity;

import com.yimi.ai.common.enums.AgentStatus;
import com.yimi.ai.common.enums.AgentStatusConverter;
import com.yimi.ai.common.enums.Department;
import com.yimi.ai.common.enums.DepartmentConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    @Convert(converter = DepartmentConverter.class)
    private Department department;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "success_rate", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal successRate = BigDecimal.ZERO;

    @Column(name = "avg_time", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal avgTime = BigDecimal.ZERO;

    @Column(name = "daily_calls", nullable = false)
    @Builder.Default
    private Integer dailyCalls = 0;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "creator_id", length = 36)
    private String creatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 20, nullable = false)
    @Convert(converter = AgentStatusConverter.class)
    @Builder.Default
    private AgentStatus status = AgentStatus.PENDING;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(columnDefinition = "JSON")
    private String skills;

    @Column(columnDefinition = "JSON")
    private String knowledge;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(columnDefinition = "JSON")
    private String examples;

    @Column(length = 50)
    @Builder.Default
    private String model = "qwen-plus";

    @Column(name = "visibility", length = 20)
    @Builder.Default
    private String visibility = "public";

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