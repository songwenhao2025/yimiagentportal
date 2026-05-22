package com.yimi.ai.knowledge.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitConfig {

    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            try {
                stmt.execute("ALTER TABLE knowledge_documents DROP FOREIGN KEY fk_knowledge_uploader;");
                System.out.println("Dropped foreign key constraint fk_knowledge_uploader");
            } catch (SQLException e) {
                System.out.println("Foreign key may not exist: " + e.getMessage());
            }
            
            try {
                stmt.execute("ALTER TABLE knowledge_documents MODIFY COLUMN uploaded_by VARCHAR(36) NULL;");
                System.out.println("Modified uploaded_by to allow NULL");
            } catch (SQLException e) {
                System.out.println("Column modification may have already been done: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}