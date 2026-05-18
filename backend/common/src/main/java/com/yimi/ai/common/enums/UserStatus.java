package com.yimi.ai.common.enums;

public enum UserStatus {
    ACTIVE("active", "活跃"),
    INACTIVE("inactive", "停用");

    private final String code;
    private final String description;

    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return ACTIVE;
    }
}