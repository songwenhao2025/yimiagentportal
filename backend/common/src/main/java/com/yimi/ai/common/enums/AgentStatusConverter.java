package com.yimi.ai.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AgentStatusConverter implements AttributeConverter<AgentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AgentStatus status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public AgentStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return AgentStatus.PENDING;
        }
        return AgentStatus.fromCode(code);
    }
}