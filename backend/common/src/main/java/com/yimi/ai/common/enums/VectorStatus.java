package com.yimi.ai.common.enums;

public enum VectorStatus {
    pending("pending", "待处理"),
    indexing("indexing", "索引中"),
    completed("completed", "已完成");

    private final String code;
    private final String description;

    VectorStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static VectorStatus fromCode(String code) {
        for (VectorStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return pending;
    }
}