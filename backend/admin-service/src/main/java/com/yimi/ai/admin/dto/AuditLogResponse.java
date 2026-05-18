package com.yimi.ai.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private String id;
    private String userId;
    private String userName;
    private String action;
    private String resource;
    private String resourceId;
    private String timestamp;
    private String ip;
    private String result;
    private String errorMessage;
}