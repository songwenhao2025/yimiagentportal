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
public class LogAnalysisResponse {

    private String summary;

    private List<Issue> issues;

    private List<String> suggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private String type;
        private String description;
        private String severity;
        private Integer count;
    }
}
