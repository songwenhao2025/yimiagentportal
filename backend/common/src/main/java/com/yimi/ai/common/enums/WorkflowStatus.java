package com.yimi.ai.common.enums;

public enum WorkflowStatus {
    DRAFT("draft", "草稿"),
    ACTIVE("active", "运行中"),
    INACTIVE("inactive", "已停用");

    private final String code;
    private final String description;

    WorkflowStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static WorkflowStatus fromCode(String code) {
        if (code == null) {
            return DRAFT;
        }
        for (WorkflowStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return DRAFT;
    }
}