package com.yimi.ai.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowGenerateRequest {

    private String description;

    private String triggerType;

    private String[] requiredSkills;
}
