package com.yimi.ai.common.enums;

public enum NodeType {
    AGENT("agent", "Agent节点"),
    SKILL("skill", "技能节点"),
    CONDITION("condition", "条件节点"),
    LOOP("loop", "循环节点"),
    APPROVAL("approval", "审批节点"),
    START("start", "开始节点"),
    END("end", "结束节点");

    private final String code;
    private final String description;

    NodeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NodeType fromCode(String code) {
        if (code == null) {
            return AGENT;
        }
        for (NodeType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return AGENT;
    }
}