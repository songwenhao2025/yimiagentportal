package com.yimi.ai.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {

    private String id;
    private String name;
    private String description;
    private String creatorId;
    private String createdAt;
    private String status;
    private String triggerType;
    private String cronExpression;
    private Integer executionCount;
    private Double successRate;
    private List<WorkflowNodeResponse> nodes;
    private List<WorkflowEdgeResponse> edges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowNodeResponse {
        private String id;
        private String type;
        private String name;
        private String config;
        private Integer x;
        private Integer y;
        private String agentId;
        private String skillId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowEdgeResponse {
        private String id;
        private String source;
        private String target;
        private String sourceHandle;
        private String targetHandle;
    }
}