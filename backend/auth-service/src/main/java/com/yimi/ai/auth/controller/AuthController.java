package com.yimi.ai.auth.controller;

import com.yimi.ai.auth.dto.LoginRequest;
import com.yimi.ai.auth.dto.LoginResponse;
import com.yimi.ai.auth.dto.UserCreateRequest;
import com.yimi.ai.auth.dto.UserResponse;
import com.yimi.ai.auth.service.AuthService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = authService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.ok(ApiResponse.error(401, "未授权"));
        }

        String token = authorization.substring(7);
        String userId = jwtUtil.extractUserId(token);
        UserResponse user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}