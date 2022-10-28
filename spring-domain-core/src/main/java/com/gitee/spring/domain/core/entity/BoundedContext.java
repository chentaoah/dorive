package com.gitee.spring.domain.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@Deprecated
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends LinkedHashMap<String, Object> {

    public BoundedContext(String... scenes) {
        put(scenes);
    }

    public void put(String... scenes) {
        for (String scene : scenes) {
            put(scene, true);
        }
    }

}
