package com.yimi.ai.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(UserStatus status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(String code) {
        if (code == null) {
            return UserStatus.ACTIVE;
        }
        return UserStatus.fromCode(code);
    }
}
