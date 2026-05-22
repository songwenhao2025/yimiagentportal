package com.yimi.ai.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCallRequest {

    private String input;
    
    private String message;
    
    private String sessionId;
    
    public String getInput() {
        if (input != null && !input.isEmpty()) {
            return input;
        }
        return message;
    }
}