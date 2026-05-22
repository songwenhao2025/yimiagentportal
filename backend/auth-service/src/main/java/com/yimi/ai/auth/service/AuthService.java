package com.yimi.ai.auth.service;

import com.yimi.ai.auth.dto.LoginRequest;
import com.yimi.ai.auth.dto.LoginResponse;
import com.yimi.ai.auth.dto.UserCreateRequest;
import com.yimi.ai.auth.dto.UserResponse;
import com.yimi.ai.auth.dto.UserUpdateRequest;
import com.yimi.ai.auth.repository.UserRepository;
import com.yimi.ai.common.entity.User;
import com.yimi.ai.common.enums.Department;
import com.yimi.ai.common.enums.UserRole;
import com.yimi.ai.common.enums.UserStatus;
import com.yimi.ai.common.exception.BusinessException;
import com.yimi.ai.common.response.PageResponse;
import com.yimi.ai.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(401, "邮箱或密码错误"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(401, "用户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getCode());
        claims.put("department", user.getDepartment().getCode());
        String token = jwtUtil.generateToken(user.getId(), claims);

        return LoginResponse.builder()
                .token(token)
                .user(convertToUserResponse(user))
                .build();
    }

    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return convertToUserResponse(user);
    }

    public PageResponse<UserResponse> listUsers(String department, String role, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (department != null && !department.isEmpty() && role != null && !role.isEmpty()) {
            Department dept = Department.fromCode(department);
            UserRole userRole = UserRole.fromCode(role);
            if (keyword != null && !keyword.isEmpty()) {
                userPage = userRepository.findByDepartmentAndRoleAndNameContaining(dept, userRole, keyword, pageable);
            } else {
                userPage = userRepository.findByDepartmentAndRole(dept, userRole, pageable);
            }
        } else if (department != null && !department.isEmpty()) {
            Department dept = Department.fromCode(department);
            if (keyword != null && !keyword.isEmpty()) {
                userPage = userRepository.findByDepartmentAndNameContaining(dept, keyword, pageable);
            } else {
                userPage = userRepository.findByDepartment(dept, pageable);
            }
        } else if (role != null && !role.isEmpty()) {
            UserRole userRole = UserRole.fromCode(role);
            if (keyword != null && !keyword.isEmpty()) {
                userPage = userRepository.findByRoleAndNameContaining(userRole, keyword, pageable);
            } else {
                userPage = userRepository.findByRole(userRole, pageable);
            }
        } else if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByNameContaining(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResponse.of(
                userPage.getContent().stream().map(this::convertToUserResponse).toList(),
                userPage.getTotalElements(),
                page,
                size
        );
    }

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return convertToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        User user = User.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .department(Department.fromCode(request.getDepartment()))
                .role(UserRole.fromCode(request.getRole()))
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        return convertToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new BusinessException(400, "邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getDepartment() != null && !request.getDepartment().isEmpty()) {
            user.setDepartment(Department.fromCode(request.getDepartment()));
        }
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(UserRole.fromCode(request.getRole()));
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            user.setStatus(UserStatus.fromCode(request.getStatus()));
        }

        user = userRepository.save(user);
        return convertToUserResponse(user);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(404, "用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void resetPassword(String id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .department(user.getDepartment().getCode())
                .role(user.getRole().getCode())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus().getCode())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}