package com.yimi.ai.common.enums;

public enum AgentStatus {
    ONLINE("online", "在线"),
    OFFLINE("offline", "离线"),
    PENDING("pending", "待审核");

    private final String code;
    private final String description;

    AgentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AgentStatus fromCode(String code) {
        if (code == null) {
            return PENDING;
        }
        for (AgentStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return PENDING;
    }
}