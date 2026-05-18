package com.yimi.ai.common.enums;

public enum TriggerType {
    API("api", "API触发"),
    CRON("cron", "定时触发"),
    EVENT("event", "事件触发");

    private final String code;
    private final String description;

    TriggerType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TriggerType fromCode(String code) {
        for (TriggerType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return API;
    }
}