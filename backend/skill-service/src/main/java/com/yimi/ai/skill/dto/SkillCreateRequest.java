package com.yimi.ai.skill.dto;

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
public class SkillCreateRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "类型不能为空")
    private String type;

    @NotBlank(message = "分类不能为空")
    private String category;

    private List<SkillParameter> parameters;

    private String returnType;

    private String version;

    private String creatorId;

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