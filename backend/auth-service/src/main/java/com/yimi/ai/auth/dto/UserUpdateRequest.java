package com.yimi.ai.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String name;

    private String email;

    private String department;

    private String role;

    private String phone;

    private String status;
}
