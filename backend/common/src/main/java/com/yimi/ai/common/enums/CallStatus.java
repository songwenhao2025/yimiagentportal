package com.yimi.ai.common.enums;

public enum CallStatus {
    SUCCESS("success"),
    FAILED("failed"),
    TIMEOUT("timeout");

    private final String code;

    CallStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CallStatus fromCode(String code) {
        for (CallStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid CallStatus code: " + code);
    }
}