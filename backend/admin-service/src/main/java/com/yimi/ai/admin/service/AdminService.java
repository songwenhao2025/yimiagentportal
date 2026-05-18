package com.yimi.ai.admin.service;

import com.yimi.ai.admin.dto.AuditLogResponse;
import com.yimi.ai.admin.dto.CostRecordResponse;
import com.yimi.ai.admin.dto.StatisticsResponse;
import com.yimi.ai.admin.repository.AuditLogRepository;
import com.yimi.ai.admin.repository.CostRecordRepository;
import com.yimi.ai.common.entity.AuditLog;
import com.yimi.ai.common.entity.CostRecord;
import com.yimi.ai.common.enums.AuditResult;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuditLogRepository auditLogRepository;
    private final CostRecordRepository costRecordRepository;

    public PageResponse<AuditLogResponse> getAuditLogs(String userId, String action, String resource, 
            String startTime, String endTime, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AuditLog> logPage;

        if (startTime != null && endTime != null) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            logPage = auditLogRepository.findByTimestampBetween(start, end, pageable);
        } else if (userId != null) {
            logPage = auditLogRepository.findByUserId(userId, pageable);
        } else if (action != null) {
            logPage = auditLogRepository.findByAction(action, pageable);
        } else if (resource != null) {
            logPage = auditLogRepository.findByResource(resource, pageable);
        } else {
            logPage = auditLogRepository.findAll(pageable);
        }

        List<AuditLogResponse> responses = logPage.getContent().stream()
                .map(this::convertToAuditLogResponse)
                .toList();

        return PageResponse.of(responses, logPage.getTotalElements(), page, size);
    }

    public PageResponse<CostRecordResponse> getCostRecords(String department, String agentId, 
            String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CostRecord> costPage;

        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            costPage = costRecordRepository.findByDateBetween(start, end, pageable);
        } else if (department != null) {
            costPage = costRecordRepository.findByDepartment(department, pageable);
        } else if (agentId != null) {
            costPage = costRecordRepository.findByAgentId(agentId, pageable);
        } else {
            costPage = costRecordRepository.findAll(pageable);
        }

        List<CostRecordResponse> responses = costPage.getContent().stream()
                .map(this::convertToCostRecordResponse)
                .toList();

        return PageResponse.of(responses, costPage.getTotalElements(), page, size);
    }

    public StatisticsResponse getStatistics(String startTime, String endTime, String department) {
        return StatisticsResponse.builder()
                .totalAgents(4L)
                .totalWorkflows(8L)
                .totalSkills(15L)
                .totalDocuments(200L)
                .totalCalls(87500L)
                .totalCost(25680.00)
                .activeUsers(50L)
                .build();
    }

    public List<DepartmentCostResponse> getDepartmentCosts(String startDate, String endDate) {
        return List.of(
            DepartmentCostResponse.builder().department("运营部").cost(8560.00).build(),
            DepartmentCostResponse.builder().department("质控部").cost(5230.00).build(),
            DepartmentCostResponse.builder().department("客服部").cost(7890.00).build(),
            DepartmentCostResponse.builder().department("财务部").cost(4000.00).build()
        );
    }

    private AuditLogResponse convertToAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userName(log.getUserName())
                .action(log.getAction())
                .resource(log.getResource())
                .resourceId(log.getResourceId())
                .timestamp(log.getTimestamp().toString())
                .ip(log.getIp())
                .result(log.getResult().getCode())
                .errorMessage(log.getErrorMessage())
                .build();
    }

    private CostRecordResponse convertToCostRecordResponse(CostRecord record) {
        return CostRecordResponse.builder()
                .id(record.getId())
                .department(record.getDepartment())
                .agentId(record.getAgentId())
                .agentName(record.getAgentName())
                .tokenUsage(record.getTokenUsage())
                .apiCalls(record.getApiCalls())
                .cost(record.getCost())
                .date(record.getDate().toString())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DepartmentCostResponse {
        private String department;
        private Double cost;
    }
}