package com.yimi.ai.common.entity;

import com.yimi.ai.common.enums.CallStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_call_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCallLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "agent_id", length = 36, nullable = false)
    private String agentId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CallStatus status;

    @Column
    private Integer duration;

    @Column(name = "token_usage")
    private Integer tokenUsage;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}