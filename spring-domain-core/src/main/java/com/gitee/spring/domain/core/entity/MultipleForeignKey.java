package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.ForeignKey;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class MultipleForeignKey implements ForeignKey {

    protected List<String> keys;

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public String getKey(int index) {
        return keys.get(index);
    }

    @Override
    public void mergeFieldValue(String fieldName, Object fieldValue) {
        if (fieldValue instanceof Collection) {
            Collection<?> fieldValues = (Collection<?>) fieldValue;
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
