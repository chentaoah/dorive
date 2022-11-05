package com.gitee.spring.domain.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends LinkedHashMap<String, Object> {

    public BoundedContext(String... keywords) {
        put(keywords);
    }

    public void put(String... keywords) {
        for (String keyword : keywords) {
            put(keyword, true);
        }
    }

}
