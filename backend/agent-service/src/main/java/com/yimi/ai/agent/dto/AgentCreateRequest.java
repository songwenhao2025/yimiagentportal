package com.yimi.ai.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCreateRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;

    private String description;

    @NotNull(message = "部门不能为空")
    private String department;

    private List<String> tags;

    @NotBlank(message = "创建者ID不能为空")
    private String creatorId;
}