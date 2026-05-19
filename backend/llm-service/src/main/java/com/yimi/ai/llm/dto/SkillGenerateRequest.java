package com.yimi.ai.llm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGenerateRequest {

    private String description;

    private String category;

    private String inputExample;

    private String outputExample;
}
