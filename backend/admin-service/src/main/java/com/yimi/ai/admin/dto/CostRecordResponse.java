package com.yimi.ai.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostRecordResponse {

    private String id;
    private String department;
    private String agentId;
    private String agentName;
    private Long tokenUsage;
    private Integer apiCalls;
    private Double cost;
    private String date;
}