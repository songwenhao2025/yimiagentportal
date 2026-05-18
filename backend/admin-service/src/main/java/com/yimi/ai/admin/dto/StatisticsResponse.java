package com.yimi.ai.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    private Long totalAgents;
    private Long totalWorkflows;
    private Long totalSkills;
    private Long totalDocuments;
    private Long totalCalls;
    private Double totalCost;
    private Long activeUsers;
}