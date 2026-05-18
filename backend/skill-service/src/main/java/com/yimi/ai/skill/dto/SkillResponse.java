package com.yimi.ai.skill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private String id;
    private String name;
    private String description;
    private String type;
    private String category;
    private List<SkillParameter> parameters;
    private String returnType;
    private String version;
    private String creatorId;
    private String createdAt;
    private String status;
    private Integer usageCount;
    private Integer timeout;
    private String apiEndpoint;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillParameter {
        private String name;
        private String type;
        private Boolean required;
        private String description;
    }
}