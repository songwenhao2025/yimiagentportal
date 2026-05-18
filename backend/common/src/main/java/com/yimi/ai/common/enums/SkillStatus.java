package com.yimi.ai.common.enums;

public enum SkillStatus {
    PUBLISHED("published", "已发布"),
    DRAFT("draft", "草稿"),
    REVIEW("review", "审核中");

    private final String code;
    private final String description;

    SkillStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SkillStatus fromCode(String code) {
        for (SkillStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return DRAFT;
    }
}