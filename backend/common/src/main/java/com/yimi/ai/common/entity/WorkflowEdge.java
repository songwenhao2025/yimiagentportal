package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_edges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEdge {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "workflow_id", length = 36, nullable = false)
    private String workflowId;

    @Column(name = "source_node_id", length = 36, nullable = false)
    private String sourceNodeId;

    @Column(name = "target_node_id", length = 36, nullable = false)
    private String targetNodeId;

    @Column(name = "source_handle", length = 50)
    private String sourceHandle;

    @Column(name = "target_handle", length = 50)
    private String targetHandle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}