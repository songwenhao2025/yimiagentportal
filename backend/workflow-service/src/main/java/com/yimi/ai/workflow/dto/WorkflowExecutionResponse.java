package com.yimi.ai.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionResponse {
    private String id;
    private String workflowId;
    private String triggeredBy;
    private String triggerType;
    private String status;
    private String startTime;
    private String endTime;
    private Long duration;
    private String inputData;
    private String outputData;
    private String errorMessage;
}