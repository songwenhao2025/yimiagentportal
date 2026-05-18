package com.yimi.ai.admin.repository;

import com.yimi.ai.common.entity.AuditLog;
import com.yimi.ai.common.entity.CostRecord;
import com.yimi.ai.common.enums.AuditResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    Page<AuditLog> findByResource(String resource, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByResult(AuditResult result, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}