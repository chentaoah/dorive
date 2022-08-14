package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class ForeignKey {

    protected ConfiguredRepository configuredRepository;
    protected Object rootEntity;
    protected List<String> keys;

    public ForeignKey(ConfiguredRepository configuredRepository, Object rootEntity) {
        this.configuredRepository = configuredRepository;
        this.rootEntity = rootEntity;
    }

    public void mergeFieldValue(String fieldName, Object fieldValue) {
        if (fieldValue instanceof Collection) {
            Collection<?> fieldValues = (Collection<?>) fieldValue;
            if (keys == null) {
                keys = new ArrayList<>(fieldValues.size());
            }
            if (keys.isEmpty()) {
                for (Object eachFieldValue : fieldValues) {
                    keys.add(fieldName + ": " + eachFieldValue);
                }
            } else {
                List<String> newKeys = new ArrayList<>(keys.size() * fieldValues.size());
                for (String existKey : keys) {
                    for (Object eachFieldValue : fieldValues) {
                        newKeys.add(existKey + ", " + fieldName + ": " + eachFieldValue);
                    }
                }
                keys = newKeys;
            }
        } else {
            if (keys == null) {
                keys = new ArrayList<>(1);
            }
            if (keys.isEmpty()) {
                keys.add(fieldName + ": " + fieldValue);
            } else {
                for (int index = 0; index < keys.size(); index++) {
                    String existKey = keys.get(index);
                    keys.set(index, existKey + ", " + fieldName + ": " + fieldValue);
                }
            }
        }
    }

}
