package com.yimi.ai.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCreateRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;

    private String description;

    @NotNull(message = "部门不能为空")
    private String department;

    private List<String> tags;

    private String creatorId;

    private String status;

    private Boolean isFavorite;

    private Double successRate;

    private Double avgTime;

    private Integer dailyCalls;

    private Integer usageCount;

    private Double rating;

    private String model;

    private String systemPrompt;

    private String visibility;

    private List<String> skills;

    private List<String> knowledge;

    private List<ExampleItem> examples;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleItem {
        private String input;
        private String output;
    }
}