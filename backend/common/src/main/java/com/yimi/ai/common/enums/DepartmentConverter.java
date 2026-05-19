package com.yimi.ai.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DepartmentConverter implements AttributeConverter<Department, String> {

    @Override
    public String convertToDatabaseColumn(Department department) {
        if (department == null) {
            return null;
        }
        return department.getCode();
    }

    @Override
    public Department convertToEntityAttribute(String code) {
        if (code == null) {
            return Department.OPERATION;
        }
        return Department.fromCode(code);
    }
}