package com.yimi.ai.auth.controller;

import com.yimi.ai.auth.dto.UserCreateRequest;
import com.yimi.ai.auth.dto.UserResponse;
import com.yimi.ai.auth.dto.UserUpdateRequest;
import com.yimi.ai.auth.service.AuthService;
import com.yimi.ai.common.response.ApiResponse;
import com.yimi.ai.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<UserResponse> response = authService.listUsers(department, role, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable String id) {
        UserResponse response = authService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = authService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = authService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        authService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable String id,
            @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(id, request.getPassword());
        return ResponseEntity.ok(ApiResponse.success());
    }

    public static class ResetPasswordRequest {
        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
