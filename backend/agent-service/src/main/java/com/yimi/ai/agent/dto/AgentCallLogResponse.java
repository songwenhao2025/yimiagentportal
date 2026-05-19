package com.yimi.ai.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCallLogResponse {
    private String id;
    private String agentId;
    private String userId;
    private String input;
    private String output;
    private String status;
    private Integer duration;
    private Integer tokenUsage;
    private String errorMessage;
    private String createdAt;
}