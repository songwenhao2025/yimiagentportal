package com.yimi.ai.admin.repository;

import com.yimi.ai.common.entity.CostRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CostRecordRepository extends JpaRepository<CostRecord, String> {

    Page<CostRecord> findByDepartment(String department, Pageable pageable);

    Page<CostRecord> findByAgentId(String agentId, Pageable pageable);

    Page<CostRecord> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<CostRecord> findByDepartmentAndDateBetween(String department, LocalDate startDate, LocalDate endDate);

    List<CostRecord> findByDate(LocalDate date);
}