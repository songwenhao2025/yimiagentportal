package com.yimi.ai.auth.config;

import com.yimi.ai.auth.repository.UserRepository;
import com.yimi.ai.common.entity.User;
import com.yimi.ai.common.enums.Department;
import com.yimi.ai.common.enums.UserRole;
import com.yimi.ai.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                
                User admin = User.builder()
                        .id(UUID.randomUUID().toString())
                        .name("张三")
                        .department(Department.IT)
                        .role(UserRole.ADMIN)
                        .email("zhangsan@company.com")
                        .phone("13800138001")
                        .password(encoder.encode("123456"))
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(admin);

                User developer = User.builder()
                        .id(UUID.randomUUID().toString())
                        .name("李四")
                        .department(Department.OPERATION)
                        .role(UserRole.DEVELOPER)
                        .email("lisi@company.com")
                        .phone("13800138002")
                        .password(encoder.encode("123456"))
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(developer);

                User user = User.builder()
                        .id(UUID.randomUUID().toString())
                        .name("王五")
                        .department(Department.CUSTOMER)
                        .role(UserRole.USER)
                        .email("wangwu@company.com")
                        .phone("13800138003")
                        .password(encoder.encode("123456"))
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(user);

                System.out.println("初始化用户数据完成");
            }
        };
    }
}