package com.gitee.dorive.api.ele;

import com.gitee.dorive.api.def.FieldDef;
import com.gitee.dorive.api.entity.FieldDefinition;
import lombok.Data;

@Data
public class FieldElement {

    private FieldDefinition fieldDefinition;
    private Class<?> genericType;
    private FieldDef fieldDef;

    public boolean isCollection() {
        return fieldDefinition.isCollection();
    }

    public String getFieldName() {
        return fieldDefinition.getFieldName();
    }

    public boolean isSameType(FieldElement fieldElement) {
        return fieldDefinition.getTypeName().equals(fieldElement.getFieldDefinition().getTypeName()) && fieldDefinition.getGenericTypeName().equals(fieldElement.getFieldDefinition().getGenericTypeName());
    }

}
