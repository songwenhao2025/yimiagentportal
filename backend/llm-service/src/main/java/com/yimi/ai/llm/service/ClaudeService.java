package com.yimi.ai.llm.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.ContentBlock;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.llm.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_MODEL = "claude-3-5-sonnet-20241022";

    public ChatResponse chat(ChatRequest request) {
        try {
            var paramsBuilder = MessageCreateParams.builder()
                    .model(request.getModel() != null ? request.getModel() : DEFAULT_MODEL);
            
            if (request.getMaxTokens() != null) {
                paramsBuilder.maxTokens(request.getMaxTokens().longValue());
            }
            if (request.getTemperature() != null) {
                paramsBuilder.temperature(request.getTemperature());
            }

            for (ChatRequest.Message msg : request.getMessages()) {
                if (msg.getRole().equalsIgnoreCase("user")) {
                    paramsBuilder.addUserMessage(msg.getContent());
                } else {
                    paramsBuilder.addAssistantMessage(msg.getContent());
                }
            }

            Message response = anthropicClient.messages().create(paramsBuilder.build());

            String contentText = extractTextFromMessage(response);
            String modelName = response.model().toString();
            String stopReasonStr = response.stopReason().map(Object::toString).orElse("end_turn");

            return ChatResponse.builder()
                    .id(response.id())
                    .model(modelName)
                    .choices(List.of(ChatResponse.Choice.builder()
                            .index(0)
                            .message(ChatResponse.Message.builder()
                                    .role("assistant")
                                    .content(contentText)
                                    .build())
                            .finishReason(stopReasonStr)
                            .build()))
                    .usage(ChatResponse.Usage.builder()
                            .promptTokens(response.usage().inputTokens())
                            .completionTokens(response.usage().outputTokens())
                            .totalTokens(response.usage().inputTokens() + response.usage().outputTokens())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Claude API call failed", e);
            throw new BusinessException(500, "调用 Claude 模型失败: " + e.getMessage());
        }
    }

    private String extractTextFromMessage(Message message) {
        if (message == null) {
            return "";
        }
        List<ContentBlock> contentBlocks = message.content();
        if (contentBlocks == null || contentBlocks.isEmpty()) {
            return "";
        }
        for (ContentBlock block : contentBlocks) {
            if (block != null && block.text().isPresent()) {
                return block.text().get().text();
            }
        }
        return "";
    }

    public SkillGenerateResponse generateSkill(SkillGenerateRequest request) {
        String prompt = String.format("""
                请根据以下描述生成一个技能定义：
                
                技能描述：%s
                技能类别：%s
                输入示例：%s
                输出示例：%s
                
                请按照以下JSON格式输出技能定义，不要添加任何额外解释：
                {
                    "name": "技能名称",
                    "description": "技能描述",
                    "category": "技能类别",
                    "parameters": [
                        {
                            "name": "参数名",
                            "type": "参数类型",
                            "description": "参数描述",
                            "required": true/false
                        }
                    ],
                    "code": "技能实现代码"
                }
                """, request.getDescription(), request.getCategory(), 
                request.getInputExample() != null ? request.getInputExample() : "",
                request.getOutputExample() != null ? request.getOutputExample() : "");

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(DEFAULT_MODEL)
                    .maxTokens(4096L)
                    .temperature(0.5)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            String jsonResponse = extractTextFromMessage(response);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new BusinessException(500, "Claude API 返回空响应");
            }

            return objectMapper.readValue(jsonResponse.trim(), SkillGenerateResponse.class);

        } catch (Exception e) {
            log.error("Skill generation failed", e);
            throw new BusinessException(500, "技能生成失败: " + e.getMessage());
        }
    }

    public WorkflowGenerateResponse generateWorkflow(WorkflowGenerateRequest request) {
        String requiredSkillsStr = request.getRequiredSkills() != null ? 
                String.join(", ", request.getRequiredSkills()) : "无";
        
        String prompt = String.format("""
                请根据以下业务流程描述，生成一个工作流编排定义：
                
                业务流程描述：%s
                触发类型：%s
                可用技能：%s
                
                请按照以下JSON格式输出工作流定义，不要添加任何额外解释：
                {
                    "name": "工作流名称",
                    "description": "工作流描述",
                    "triggerType": "触发类型",
                    "nodes": [
                        {
                            "id": "节点ID",
                            "type": "skill/external/condition",
                            "skillId": "技能ID（如果是技能节点）",
                            "skillName": "技能名称",
                            "description": "节点描述",
                            "positionX": 100,
                            "positionY": 100
                        }
                    ],
                    "edges": [
                        {
                            "id": "连线ID",
                            "source": "源节点ID",
                            "target": "目标节点ID",
                            "condition": "条件表达式（可选）"
                        }
                    ]
                }
                """, request.getDescription(), 
                request.getTriggerType() != null ? request.getTriggerType() : "manual",
                requiredSkillsStr);

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(DEFAULT_MODEL)
                    .maxTokens(4096L)
                    .temperature(0.5)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            String jsonResponse = extractTextFromMessage(response);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new BusinessException(500, "Claude API 返回空响应");
            }

            return objectMapper.readValue(jsonResponse.trim(), WorkflowGenerateResponse.class);

        } catch (Exception e) {
            log.error("Workflow generation failed", e);
            throw new BusinessException(500, "工作流生成失败: " + e.getMessage());
        }
    }

    public KnowledgeQAResponse knowledgeQA(KnowledgeQARequest request) {
        StringBuilder docsBuilder = new StringBuilder();
        for (int i = 0; i < request.getDocuments().size(); i++) {
            docsBuilder.append(String.format("文档%d：\n%s\n\n", i + 1, request.getDocuments().get(i)));
        }

        String prompt = String.format("""
                请根据以下参考文档回答问题：
                
                参考文档：
                %s
                
                问题：%s
                
                请按照以下JSON格式输出答案，不要添加任何额外解释：
                {
                    "answer": "答案内容",
                    "references": [
                        {
                            "documentIndex": 文档索引（从1开始）,
                            "contentSnippet": "引用的文档片段",
                            "relevanceScore": 相关度分数（0-100）
                        }
                    ],
                    "confidence": 置信度（0-1）
                }
                """, docsBuilder.toString(), request.getQuestion());

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(DEFAULT_MODEL)
                    .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens().longValue() : 4096L)
                    .temperature(0.3)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            String jsonResponse = extractTextFromMessage(response);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new BusinessException(500, "Claude API 返回空响应");
            }

            return objectMapper.readValue(jsonResponse.trim(), KnowledgeQAResponse.class);

        } catch (Exception e) {
            log.error("Knowledge QA failed", e);
            throw new BusinessException(500, "知识库问答失败: " + e.getMessage());
        }
    }

    public LogAnalysisResponse analyzeLogs(LogAnalysisRequest request) {
        StringBuilder logsBuilder = new StringBuilder();
        for (String log : request.getLogs()) {
            logsBuilder.append(log).append("\n");
        }

        String analysisType = request.getAnalysisType() != null ? request.getAnalysisType() : "general";
        
        String prompt = String.format("""
                请分析以下系统日志：
                
                日志内容：
                %s
                
                分析类型：%s
                
                请按照以下JSON格式输出分析结果，不要添加任何额外解释：
                {
                    "summary": "日志摘要",
                    "issues": [
                        {
                            "type": "问题类型（如错误、警告、性能问题）",
                            "description": "问题描述",
                            "severity": "严重程度（critical/high/medium/low）",
                            "count": 出现次数
                        }
                    ],
                    "suggestions": ["优化建议1", "优化建议2"]
                }
                """, logsBuilder.toString(), analysisType);

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(DEFAULT_MODEL)
                    .maxTokens(2048L)
                    .temperature(0.3)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            String jsonResponse = extractTextFromMessage(response);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new BusinessException(500, "Claude API 返回空响应");
            }

            return objectMapper.readValue(jsonResponse.trim(), LogAnalysisResponse.class);

        } catch (Exception e) {
            log.error("Log analysis failed", e);
            throw new BusinessException(500, "日志分析失败: " + e.getMessage());
        }
    }

    public String summarize(String text, int maxLength) {
        String prompt = String.format("""
                请对以下文本进行总结，要求：
                1. 保留核心信息
                2. 不超过 %d 字
                3. 语言简洁明了
                
                文本内容：
                %s
                """, maxLength, text);

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(DEFAULT_MODEL)
                    .maxTokens(1024L)
                    .temperature(0.3)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            return extractTextFromMessage(response).trim();

        } catch (Exception e) {
            log.error("Summarization failed", e);
            throw new BusinessException(500, "文本总结失败: " + e.getMessage());
        }
    }

    public String extractKeywords(String text, int count) {
        String prompt = String.format("""
                请从以下文本中提取最多 %d 个关键词，用逗号分隔：
                
                文本内容：
                %s
                """, count, text);

        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(DEFAULT_MODEL)
                    .maxTokens(256L)
                    .temperature(0.1)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            return extractTextFromMessage(response).trim();

        } catch (Exception e) {
            log.error("Keyword extraction failed", e);
            throw new BusinessException(500, "关键词提取失败: " + e.getMessage());
        }
    }
}