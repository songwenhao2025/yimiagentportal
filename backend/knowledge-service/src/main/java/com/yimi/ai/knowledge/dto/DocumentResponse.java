package com.yimi.ai.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private String id;
    private String title;
    private String type;
    private Long size;
    private String uploadedBy;
    private String uploadedAt;
    private String status;
    private String vectorStatus;
    private Integer chunkCount;
    private Integer hitCount;
    private String category;
    private String filePath;
    private String fileUrl;
    private String content;
}