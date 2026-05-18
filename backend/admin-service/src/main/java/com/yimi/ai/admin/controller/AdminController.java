package com.yimi.ai.admin.controller;

import com.yimi.ai.admin.dto.AuditLogResponse;
import com.yimi.ai.admin.dto.CostRecordResponse;
import com.yimi.ai.admin.dto.StatisticsResponse;
import com.yimi.ai.admin.service.AdminService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<AuditLogResponse> response = adminService.getAuditLogs(userId, action, resource, startTime, endTime, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/costs")
    public ResponseEntity<ApiResponse<PageResponse<CostRecordResponse>>> getCostRecords(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageResponse<CostRecordResponse> response = adminService.getCostRecords(department, agentId, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String department) {
        
        StatisticsResponse response = adminService.getStatistics(startTime, endTime, department);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getDepartments() {
        List<DepartmentResponse> departments = List.of(
            new DepartmentResponse("operation", "运营部"),
            new DepartmentResponse("qc", "质控部"),
            new DepartmentResponse("customer", "客服部"),
            new DepartmentResponse("finance", "财务部"),
            new DepartmentResponse("it", "IT部")
        );
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {
        List<RoleResponse> roles = List.of(
            new RoleResponse("admin", "管理员", "系统管理员，拥有全部权限"),
            new RoleResponse("developer", "开发者", "可以创建和管理Agent"),
            new RoleResponse("user", "普通用户", "可以使用Agent"),
            new RoleResponse("operator", "操作员", "可以执行特定操作")
        );
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DepartmentResponse {
        private String id;
        private String name;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleResponse {
        private String id;
        private String name;
        private String description;
    }
}