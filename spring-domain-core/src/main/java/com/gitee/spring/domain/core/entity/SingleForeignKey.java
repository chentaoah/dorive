package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.ForeignKey;

public class SingleForeignKey implements ForeignKey {

    protected String key;

    @Override
    public int size() {
        return key != null ? 1 : 0;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String getKey(int index) {
        return index == 0 ? key : null;
    }

    @Override
    public void mergeFieldValue(String fieldName, Object fieldValue) {
        key = fieldName + ": " + fieldValue;
    }

}
