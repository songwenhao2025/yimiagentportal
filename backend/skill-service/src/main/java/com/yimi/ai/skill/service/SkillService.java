package com.yimi.ai.skill.service;

import com.yimi.ai.skill.dto.SkillCreateRequest;
import com.yimi.ai.skill.dto.SkillResponse;
import com.yimi.ai.skill.repository.SkillRepository;
import com.yimi.ai.common.entity.Skill;
import com.yimi.ai.common.enums.SkillStatus;
import com.yimi.ai.common.enums.SkillType;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResponse<SkillResponse> list(String category, String status, String type, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Skill> skillPage;

        if (keyword != null && !keyword.isEmpty()) {
            skillPage = skillRepository.findByNameContaining(keyword, pageable);
        } else if (category != null) {
            skillPage = skillRepository.findByCategory(category, pageable);
        } else if (status != null) {
            skillPage = skillRepository.findByStatus(SkillStatus.fromCode(status), pageable);
        } else if (type != null) {
            skillPage = skillRepository.findByType(SkillType.fromCode(type), pageable);
        } else {
            skillPage = skillRepository.findAll(pageable);
        }

        List<SkillResponse> responses = skillPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, skillPage.getTotalElements(), page, size);
    }

    public SkillResponse get(String id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "技能不存在"));
        return convertToResponse(skill);
    }

    public SkillResponse create(SkillCreateRequest request) {
        Skill skill = Skill.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .type(SkillType.fromCode(request.getType()))
                .category(request.getCategory())
                .parameters(serializeParameters(request.getParameters()))
                .returnType(request.getReturnType())
                .version(request.getVersion() != null ? request.getVersion() : "1.0.0")
                .creatorId(request.getCreatorId())
                .status(SkillStatus.DRAFT)
                .usageCount(0)
                .timeout(request.getTimeout() != null ? request.getTimeout() : 30)
                .apiEndpoint(request.getApiEndpoint())
                .build();

        Skill saved = skillRepository.save(skill);
        return convertToResponse(saved);
    }

    public SkillResponse update(String id, SkillCreateRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "技能不存在"));

        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setType(SkillType.fromCode(request.getType()));
        skill.setCategory(request.getCategory());
        skill.setParameters(serializeParameters(request.getParameters()));
        skill.setReturnType(request.getReturnType());
        skill.setVersion(request.getVersion());
        skill.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30);
        skill.setApiEndpoint(request.getApiEndpoint());

        Skill saved = skillRepository.save(skill);
        return convertToResponse(saved);
    }

    public void delete(String id) {
        if (!skillRepository.existsById(id)) {
            throw new BusinessException(404, "技能不存在");
        }
        skillRepository.deleteById(id);
    }

    public SkillResponse publish(String id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "技能不存在"));
        
        if (skill.getStatus() == SkillStatus.PUBLISHED) {
            throw new BusinessException(400, "技能已发布");
        }

        skill.setStatus(SkillStatus.PUBLISHED);
        Skill saved = skillRepository.save(skill);
        return convertToResponse(saved);
    }

    private SkillResponse convertToResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .type(skill.getType().getCode())
                .category(skill.getCategory())
                .parameters(deserializeParameters(skill.getParameters()))
                .returnType(skill.getReturnType())
                .version(skill.getVersion())
                .creatorId(skill.getCreatorId())
                .createdAt(skill.getCreatedAt().toString())
                .status(skill.getStatus().getCode())
                .usageCount(skill.getUsageCount())
                .timeout(skill.getTimeout())
                .apiEndpoint(skill.getApiEndpoint())
                .build();
    }

    private String serializeParameters(List<SkillCreateRequest.SkillParameter> parameters) {
        try {
            return objectMapper.writeValueAsString(parameters);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<SkillResponse.SkillParameter> deserializeParameters(String parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<SkillCreateRequest.SkillParameter> params = objectMapper.readValue(
                    parameters, 
                    new TypeReference<List<SkillCreateRequest.SkillParameter>>() {}
            );
            return params.stream()
                    .map(p -> new SkillResponse.SkillParameter(p.getName(), p.getType(), p.getRequired(), p.getDescription()))
                    .toList();
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}