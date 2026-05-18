package com.yimi.ai.common.enums;

public enum UserRole {
    ADMIN("admin", "管理员"),
    DEVELOPER("developer", "开发者"),
    USER("user", "普通用户"),
    OPERATOR("operator", "操作员");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER;
    }
}