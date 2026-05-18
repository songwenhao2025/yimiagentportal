package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_nodes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNode {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "workflow_id", length = 36, nullable = false)
    private String workflowId;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private com.yimi.ai.common.enums.NodeType type;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "JSON")
    private String config;

    @Column(name = "position_x", nullable = false)
    @Builder.Default
    private Integer positionX = 0;

    @Column(name = "position_y", nullable = false)
    @Builder.Default
    private Integer positionY = 0;

    @Column(name = "agent_id", length = 36)
    private String agentId;

    @Column(name = "skill_id", length = 36)
    private String skillId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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