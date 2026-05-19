package com.yimi.ai.agent.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner removeForeignKeyConstraint() {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE agents DROP FOREIGN KEY fk_agents_creator");
                System.out.println("Removed foreign key constraint fk_agents_creator");
            } catch (Exception e) {
                System.out.println("Foreign key constraint may not exist or already removed: " + e.getMessage());
            }
        };
    }
}