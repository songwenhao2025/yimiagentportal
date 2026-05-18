package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cost_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostRecord {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 50, nullable = false)
    private String department;

    @Column(name = "agent_id", length = 36, nullable = false)
    private String agentId;

    @Column(name = "agent_name", length = 100, nullable = false)
    private String agentName;

    @Column(name = "token_usage", nullable = false)
    @Builder.Default
    private Long tokenUsage = 0L;

    @Column(name = "api_calls", nullable = false)
    @Builder.Default
    private Integer apiCalls = 0;

    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private Double cost = 0.0;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}