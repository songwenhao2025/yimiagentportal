package com.yimi.ai.common.enums;

public enum DocumentStatus {
    UPLOADING("uploading", "上传中"),
    PROCESSING("processing", "处理中"),
    READY("ready", "已就绪"),
    FAILED("failed", "失败");

    private final String code;
    private final String description;

    DocumentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DocumentStatus fromCode(String code) {
        for (DocumentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return UPLOADING;
    }
}