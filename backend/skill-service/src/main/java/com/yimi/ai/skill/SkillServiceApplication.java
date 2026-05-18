package com.yimi.ai.skill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.yimi.ai.skill", "com.yimi.ai.common"})
@EntityScan(basePackages = {"com.yimi.ai.common.entity"})
public class SkillServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillServiceApplication.class, args);
    }
}