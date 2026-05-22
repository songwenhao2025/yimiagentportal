package com.yimi.ai.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {

    public static void main(String[] args) {
        String url = "jdbc:mysql://10.206.20.129:3306/yimi_ai_portal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "0AHPeevq2U2xWTi*";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected to database");
            
            try {
                stmt.execute("ALTER TABLE skills DROP FOREIGN KEY fk_skills_creator;");
                System.out.println("Dropped foreign key constraint fk_skills_creator");
            } catch (SQLException e) {
                System.out.println("Foreign key may not exist: " + e.getMessage());
            }
            
            stmt.execute("ALTER TABLE skills MODIFY COLUMN creator_id VARCHAR(36) NULL;");
            System.out.println("Modified creator_id to allow NULL");
            
            System.out.println("Done!");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
