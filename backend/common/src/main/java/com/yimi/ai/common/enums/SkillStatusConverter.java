package com.yimi.ai.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SkillStatusConverter implements AttributeConverter<SkillStatus, String> {

    @Override
    public String convertToDatabaseColumn(SkillStatus status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public SkillStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return SkillStatus.DRAFT;
        }
        return SkillStatus.fromCode(code);
    }
}