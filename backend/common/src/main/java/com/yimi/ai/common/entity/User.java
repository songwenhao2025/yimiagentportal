package com.yimi.ai.common.entity;

import com.yimi.ai.common.enums.Department;
import com.yimi.ai.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 20, unique = true, nullable = false)
    private String phone;

    @Column(length = 255)
    private String password;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.yimi.ai.common.enums.UserStatus status = com.yimi.ai.common.enums.UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}