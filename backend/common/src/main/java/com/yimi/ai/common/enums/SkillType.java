package com.yimi.ai.common.enums;

public enum SkillType {
    API("api", "API接口"),
    FUNCTION("function", "云函数");

    private final String code;
    private final String description;

    SkillType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SkillType fromCode(String code) {
        for (SkillType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return FUNCTION;
    }
}