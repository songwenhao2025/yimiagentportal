package com.yimi.ai.auth.repository;

import com.yimi.ai.common.entity.User;
import com.yimi.ai.common.enums.Department;
import com.yimi.ai.common.enums.UserRole;
import com.yimi.ai.common.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Page<User> findByNameContaining(String keyword, Pageable pageable);

    Page<User> findByDepartment(Department department, Pageable pageable);

    Page<User> findByDepartmentAndNameContaining(Department department, String keyword, Pageable pageable);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndNameContaining(UserRole role, String keyword, Pageable pageable);

    Page<User> findByDepartmentAndRole(Department department, UserRole role, Pageable pageable);

    Page<User> findByDepartmentAndRoleAndNameContaining(Department department, UserRole role, String keyword, Pageable pageable);
}