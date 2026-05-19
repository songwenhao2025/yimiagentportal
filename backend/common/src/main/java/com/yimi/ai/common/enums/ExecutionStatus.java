package com.yimi.ai.common.enums;

public enum ExecutionStatus {
    RUNNING("running"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String code;

    ExecutionStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ExecutionStatus fromCode(String code) {
        for (ExecutionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ExecutionStatus code: " + code);
    }
}