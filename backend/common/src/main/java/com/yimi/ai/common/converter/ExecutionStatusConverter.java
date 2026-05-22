package com.yimi.ai.common.converter;

import com.yimi.ai.common.enums.ExecutionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExecutionStatusConverter implements AttributeConverter<ExecutionStatus, String> {

    @Override
    public String convertToDatabaseColumn(ExecutionStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public ExecutionStatus convertToEntityAttribute(String code) {
        return ExecutionStatus.fromCode(code);
    }
}