package com.yimi.ai.common.enums;

public enum Department {
    OPERATION("operation", "运营部"),
    QC("qc", "质控部"),
    CUSTOMER("customer", "客服部"),
    FINANCE("finance", "财务部"),
    IT("it", "IT部");

    private final String code;
    private final String description;

    Department(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Department fromCode(String code) {
        for (Department dept : values()) {
            if (dept.code.equals(code)) {
                return dept;
            }
        }
        return OPERATION;
    }
}