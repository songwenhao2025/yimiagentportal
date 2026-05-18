package com.yimi.ai.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(length = 100, nullable = false)
    private String action;

    @Column(length = 50, nullable = false)
    private String resource;

    @Column(name = "resource_id", length = 36)
    private String resourceId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 50)
    private String ip;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private com.yimi.ai.common.enums.AuditResult result;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}