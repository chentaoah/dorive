package com.gitee.spring.domain.core.api;

public interface ForeignKey {

    int size();

    boolean isEmpty();

    String getKey(int index);

    void mergeFieldValue(String fieldName, Object fieldValue);

}
