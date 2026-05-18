package com.yimi.ai.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    private String id;
    private String name;
    private String description;
    private String department;
    private List<String> tags;
    private Double successRate;
    private Double avgTime;
    private Integer dailyCalls;
    private Integer usageCount;
    private String creatorId;
    private String createdAt;
    private String status;
    private Boolean isFavorite;
    private Double rating;
}