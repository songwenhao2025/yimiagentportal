package com.yimi.ai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "类型不能为空")
    private String type;

    private Long size;

    private String uploadedBy;

    @NotBlank(message = "分类不能为空")
    private String category;

    private String filePath;

    private String fileUrl;

    private String content;
}