package com.yimi.ai.common.enums;

public enum DocumentType {
    pdf("pdf", "PDF文件"),
    word("word", "Word文档"),
    excel("excel", "Excel表格"),
    markdown("markdown", "Markdown文件"),
    url("url", "网页链接");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DocumentType fromCode(String code) {
        for (DocumentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return pdf;
    }
}