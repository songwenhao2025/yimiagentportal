package com.yimi.ai.agent.service;

import com.yimi.ai.agent.dto.AgentRatingRequest;
import com.yimi.ai.agent.dto.AgentRatingResponse;
import com.yimi.ai.agent.repository.AgentRatingRepository;
import com.yimi.ai.common.entity.AgentRating;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentRatingService {

    private final AgentRatingRepository ratingRepository;

    public AgentRatingResponse create(String agentId, String userId, AgentRatingRequest request) {
        AgentRating existing = ratingRepository.findByAgentIdAndUserId(agentId, userId).orElse(null);
        if (existing != null) {
            throw new BusinessException(400, "您已对该Agent进行过评分");
        }

        AgentRating rating = AgentRating.builder()
                .id(UUID.randomUUID().toString())
                .agentId(agentId)
                .userId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        AgentRating saved = ratingRepository.save(rating);
        return convertToResponse(saved);
    }

    public AgentRatingResponse update(String agentId, String userId, AgentRatingRequest request) {
        AgentRating rating = ratingRepository.findByAgentIdAndUserId(agentId, userId)
                .orElseThrow(() -> new BusinessException(404, "评分记录不存在"));

        rating.setRating(request.getRating());
        rating.setComment(request.getComment());

        AgentRating saved = ratingRepository.save(rating);
        return convertToResponse(saved);
    }

    public void delete(String id) {
        if (!ratingRepository.existsById(id)) {
            throw new BusinessException(404, "评分记录不存在");
        }
        ratingRepository.deleteById(id);
    }

    public PageResponse<AgentRatingResponse> list(String agentId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AgentRating> ratingPage = ratingRepository.findByAgentId(agentId, pageable);

        List<AgentRatingResponse> responses = ratingPage.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        return PageResponse.of(responses, ratingPage.getTotalElements(), page, size);
    }

    private AgentRatingResponse convertToResponse(AgentRating rating) {
        return AgentRatingResponse.builder()
                .id(rating.getId())
                .agentId(rating.getAgentId())
                .userId(rating.getUserId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt().toString())
                .build();
    }
}