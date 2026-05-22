package com.yimi.ai.common.converter;

import com.yimi.ai.common.enums.WorkflowStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WorkflowStatusConverter implements AttributeConverter<WorkflowStatus, String> {

    @Override
    public String convertToDatabaseColumn(WorkflowStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public WorkflowStatus convertToEntityAttribute(String code) {
        return WorkflowStatus.fromCode(code);
    }
}