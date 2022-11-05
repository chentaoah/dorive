package com.gitee.spring.domain.core3.entity;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Data
public class Property {

    private Field declaredField;
    private Class<?> fieldClass;
    private boolean collection;
    private Class<?> genericFieldClass;
    private String fieldName;

    public Property(Field declaredField) {
        Class<?> fieldClass = declaredField.getType();
        boolean isCollection = false;
        Class<?> fieldGenericClass = fieldClass;
        String fieldName = declaredField.getName();

        if (Collection.class.isAssignableFrom(fieldClass)) {
            isCollection = true;
            ParameterizedType parameterizedType = (ParameterizedType) declaredField.getGenericType();
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            fieldGenericClass = (Class<?>) actualTypeArgument;
        }

        this.declaredField = declaredField;
        this.fieldClass = fieldClass;
        this.collection = isCollection;
        this.genericFieldClass = fieldGenericClass;
        this.fieldName = fieldName;
    }

    public boolean isSameType(Property property) {
        return fieldClass == property.getFieldClass() && genericFieldClass == property.getGenericFieldClass();
    }

}
