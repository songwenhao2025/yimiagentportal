package com.yimi.ai.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRatingResponse {
    private String id;
    private String agentId;
    private String userId;
    private Double rating;
    private String comment;
    private String createdAt;
}