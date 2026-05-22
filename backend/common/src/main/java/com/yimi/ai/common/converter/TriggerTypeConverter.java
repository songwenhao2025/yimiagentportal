package com.yimi.ai.common.converter;

import com.yimi.ai.common.enums.TriggerType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TriggerTypeConverter implements AttributeConverter<TriggerType, String> {

    @Override
    public String convertToDatabaseColumn(TriggerType type) {
        return type != null ? type.getCode() : null;
    }

    @Override
    public TriggerType convertToEntityAttribute(String code) {
        return TriggerType.fromCode(code);
    }
}