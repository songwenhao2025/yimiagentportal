package com.yimi.ai.agent.config;

import com.yimi.ai.agent.repository.AgentRepository;
import com.yimi.ai.common.entity.Agent;
import com.yimi.ai.common.enums.AgentStatus;
import com.yimi.ai.common.enums.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AgentRepository agentRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (agentRepository.count() == 0) {
                Agent agent1 = Agent.builder()
                        .id(UUID.randomUUID().toString())
                        .name("智能客服助手")
                        .description("基于大语言模型的智能客服系统，能够自动回答客户问题")
                        .department(Department.CUSTOMER)
                        .tags("[\"客服\", \"AI\", \"智能\"]")
                        .creatorId("admin")
                        .status(AgentStatus.ONLINE)
                        .isFavorite(true)
                        .successRate(95.5)
                        .avgTime(2.5)
                        .dailyCalls(1200)
                        .usageCount(36500)
                        .rating(4.8)
                        .build();
                agentRepository.save(agent1);

                Agent agent2 = Agent.builder()
                        .id(UUID.randomUUID().toString())
                        .name("财务报表分析")
                        .description("自动生成财务报表和数据分析报告")
                        .department(Department.FINANCE)
                        .tags("[\"财务\", \"报表\", \"分析\"]")
                        .creatorId("admin")
                        .status(AgentStatus.ONLINE)
                        .isFavorite(false)
                        .successRate(98.2)
                        .avgTime(5.2)
                        .dailyCalls(500)
                        .usageCount(18250)
                        .rating(4.9)
                        .build();
                agentRepository.save(agent2);

                Agent agent3 = Agent.builder()
                        .id(UUID.randomUUID().toString())
                        .name("质量检测助手")
                        .description("产品质量检测和缺陷识别系统")
                        .department(Department.QC)
                        .tags("[\"质检\", \"质量\", \"检测\"]")
                        .creatorId("admin")
                        .status(AgentStatus.PENDING)
                        .isFavorite(false)
                        .successRate(0.0)
                        .avgTime(0.0)
                        .dailyCalls(0)
                        .usageCount(0)
                        .rating(null)
                        .build();
                agentRepository.save(agent3);

                Agent agent4 = Agent.builder()
                        .id(UUID.randomUUID().toString())
                        .name("运营数据分析")
                        .description("运营数据可视化和趋势分析")
                        .department(Department.OPERATION)
                        .tags("[\"运营\", \"数据\", \"分析\"]")
                        .creatorId("admin")
                        .status(AgentStatus.ONLINE)
                        .isFavorite(true)
                        .successRate(92.8)
                        .avgTime(3.8)
                        .dailyCalls(800)
                        .usageCount(29200)
                        .rating(4.7)
                        .build();
                agentRepository.save(agent4);

                System.out.println("初始化Agent数据完成");
            }
        };
    }
}