package com.yimi.ai.knowledge.controller;

import com.yimi.ai.knowledge.dto.DocumentCreateRequest;
import com.yimi.ai.knowledge.dto.DocumentResponse;
import com.yimi.ai.knowledge.service.KnowledgeService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DocumentResponse>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<DocumentResponse> response = knowledgeService.list(category, status, type, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> get(@PathVariable String id) {
        DocumentResponse response = knowledgeService.get(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> create(@Valid @RequestBody DocumentCreateRequest request) {
        DocumentResponse response = knowledgeService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> update(
            @PathVariable String id, 
            @Valid @RequestBody DocumentCreateRequest request) {
        DocumentResponse response = knowledgeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        knowledgeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        List<DocumentResponse> response = knowledgeService.search(query, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = List.of(
            new CategoryResponse("1", "操作手册", 45),
            new CategoryResponse("2", "FAQ", 128),
            new CategoryResponse("3", "财务文档", 32),
            new CategoryResponse("4", "技术文档", 67),
            new CategoryResponse("5", "标准规范", 23)
        );
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CategoryResponse {
        private String id;
        private String name;
        private int count;
    }
}