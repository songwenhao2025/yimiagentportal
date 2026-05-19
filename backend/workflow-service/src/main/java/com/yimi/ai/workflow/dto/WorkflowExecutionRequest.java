package com.yimi.ai.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionRequest {
    private String workflowId;
    private String triggeredBy;
    private String triggerType;
    private String inputData;
}