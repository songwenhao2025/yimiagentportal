package com.yimi.ai.common.enums;

public enum DocumentStatus {
    uploading("uploading", "上传中"),
    processing("processing", "处理中"),
    ready("ready", "已就绪"),
    failed("failed", "失败");

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
        return uploading;
    }
}