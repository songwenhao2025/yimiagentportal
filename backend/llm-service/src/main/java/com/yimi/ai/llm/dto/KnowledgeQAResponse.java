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
public class KnowledgeQAResponse {

    private String answer;

    private List<Reference> references;

    private Double confidence;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reference {
        private Integer documentIndex;
        private String contentSnippet;
        private Integer relevanceScore;
    }
}
