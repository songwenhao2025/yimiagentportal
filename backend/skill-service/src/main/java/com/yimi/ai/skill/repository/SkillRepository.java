package com.yimi.ai.skill.repository;

import com.yimi.ai.common.entity.Skill;
import com.yimi.ai.common.enums.SkillStatus;
import com.yimi.ai.common.enums.SkillType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, String> {

    Page<Skill> findByCategory(String category, Pageable pageable);

    Page<Skill> findByStatus(SkillStatus status, Pageable pageable);

    Page<Skill> findByType(SkillType type, Pageable pageable);

    Page<Skill> findByNameContaining(String name, Pageable pageable);

    List<Skill> findByCreatorId(String creatorId);

    List<Skill> findByStatus(SkillStatus status);
}