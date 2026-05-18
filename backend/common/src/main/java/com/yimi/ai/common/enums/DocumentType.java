package com.yimi.ai.common.enums;

public enum DocumentType {
    PDF("pdf", "PDF文件"),
    WORD("word", "Word文档"),
    EXCEL("excel", "Excel表格"),
    MARKDOWN("markdown", "Markdown文件"),
    URL("url", "网页链接");

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
        return PDF;
    }
}