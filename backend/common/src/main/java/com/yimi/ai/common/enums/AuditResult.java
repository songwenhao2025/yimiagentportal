package com.yimi.ai.common.enums;

public enum AuditResult {
    SUCCESS("success", "成功"),
    FAILED("failed", "失败");

    private final String code;
    private final String description;

    AuditResult(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AuditResult fromCode(String code) {
        for (AuditResult result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        return SUCCESS;
    }
}