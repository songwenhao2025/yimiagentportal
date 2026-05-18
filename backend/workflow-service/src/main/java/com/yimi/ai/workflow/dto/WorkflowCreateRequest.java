package com.yimi.ai.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCreateRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "触发方式不能为空")
    private String triggerType;

    private String cronExpression;

    @NotBlank(message = "创建者ID不能为空")
    private String creatorId;

    private List<WorkflowNodeRequest> nodes;

    private List<WorkflowEdgeRequest> edges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowNodeRequest {
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowEdgeRequest {
        private String id;
        private String source;
        private String target;
        private String sourceHandle;
        private String targetHandle;
    }
}