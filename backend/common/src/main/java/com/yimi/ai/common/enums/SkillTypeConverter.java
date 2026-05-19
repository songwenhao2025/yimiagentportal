package com.yimi.ai.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SkillTypeConverter implements AttributeConverter<SkillType, String> {

    @Override
    public String convertToDatabaseColumn(SkillType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public SkillType convertToEntityAttribute(String code) {
        if (code == null) {
            return SkillType.FUNCTION;
        }
        return SkillType.fromCode(code);
    }
}