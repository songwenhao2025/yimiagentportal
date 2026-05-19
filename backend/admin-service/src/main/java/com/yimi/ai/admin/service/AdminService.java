package com.yimi.ai.admin.service;

import com.yimi.ai.admin.dto.AuditLogResponse;
import com.yimi.ai.admin.dto.CostRecordResponse;
import com.yimi.ai.admin.dto.StatisticsResponse;
import com.yimi.ai.admin.repository.AgentRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuditLogRepository auditLogRepository;
    private final CostRecordRepository costRecordRepository;
    private final AgentRepository agentRepository;

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

    public StatisticsResponse getStatistics(String startTime, String endTime) {
        long totalAgents = agentRepository.countAll();
        long totalCalls = agentRepository.sumUsageCount();

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startTime != null && endTime != null) {
            start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            end = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        BigDecimal totalCost;
        if (start != null && end != null) {
            totalCost = costRecordRepository.sumCostByDateBetween(start.toLocalDate(), end.toLocalDate());
        } else {
            totalCost = costRecordRepository.sumAllCost();
        }

        long activeUsers = agentRepository.countActiveUsers(
                start != null ? start : LocalDateTime.now().minusDays(30),
                end != null ? end : LocalDateTime.now()
        );

        return StatisticsResponse.builder()
                .totalAgents(totalAgents)
                .totalWorkflows(0L)
                .totalSkills(0L)
                .totalDocuments(0L)
                .totalCalls(totalCalls)
                .totalCost(totalCost != null ? totalCost.doubleValue() : 0.0)
                .activeUsers(activeUsers)
                .build();
    }

    public List<DepartmentCostResponse> getDepartmentCosts(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        List<Object[]> results = costRecordRepository.sumCostByDepartment(start, end);
        List<DepartmentCostResponse> responses = new ArrayList<>();
        for (Object[] row : results) {
            String dept = (String) row[0];
            BigDecimal cost = (BigDecimal) row[1];
            responses.add(DepartmentCostResponse.builder()
                    .department(dept)
                    .cost(cost.doubleValue())
                    .build());
        }
        return responses;
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
                .cost(record.getCost() != null ? record.getCost().doubleValue() : null)
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