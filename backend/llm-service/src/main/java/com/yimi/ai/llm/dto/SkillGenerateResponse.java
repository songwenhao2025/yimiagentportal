package com.yimi.ai.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGenerateResponse {

    private String name;

    private String description;

    private String category;

    private List<Parameter> parameters;

    private String code;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String name;
        private String type;
        private String description;
        private Boolean required;
    }
}
