package com.yimi.ai.skill.controller;

import com.yimi.ai.skill.dto.SkillCreateRequest;
import com.yimi.ai.skill.dto.SkillResponse;
import com.yimi.ai.skill.service.SkillService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SkillResponse>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<SkillResponse> response = skillService.list(category, status, type, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> get(@PathVariable String id) {
        SkillResponse response = skillService.get(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SkillResponse>> create(@Valid @RequestBody SkillCreateRequest request) {
        SkillResponse response = skillService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> update(
            @PathVariable String id, 
            @Valid @RequestBody SkillCreateRequest request) {
        SkillResponse response = skillService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        skillService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<SkillResponse>> publish(@PathVariable String id) {
        SkillResponse response = skillService.publish(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}