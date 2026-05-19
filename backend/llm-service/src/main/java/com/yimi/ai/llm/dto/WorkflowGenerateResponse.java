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
public class WorkflowGenerateResponse {

    private String name;

    private String description;

    private String triggerType;

    private List<Node> nodes;

    private List<Edge> edges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private String id;
        private String type;
        private String skillId;
        private String skillName;
        private String description;
        private Integer positionX;
        private Integer positionY;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private String id;
        private String source;
        private String target;
        private String condition;
    }
}
