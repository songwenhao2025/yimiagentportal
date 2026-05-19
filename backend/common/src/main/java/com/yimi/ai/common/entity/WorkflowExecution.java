package com.yimi.ai.common.entity;

import com.yimi.ai.common.enums.ExecutionStatus;
import com.yimi.ai.common.enums.TriggerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "workflow_id", length = 36, nullable = false)
    private String workflowId;

    @Column(name = "triggered_by", length = 36)
    private String triggeredBy;

    @Column(name = "trigger_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Long duration;

    @Column(name = "input_data", columnDefinition = "JSON")
    private String inputData;

    @Column(name = "output_data", columnDefinition = "JSON")
    private String outputData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
    }
}