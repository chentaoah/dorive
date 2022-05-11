package com.gitee.spring.domain.core.api;

public interface ParameterConverter {

    String convertFieldName(String fieldName, Object fieldValue);

    Object convertFieldValue(String fieldName, Object fieldValue);

}
