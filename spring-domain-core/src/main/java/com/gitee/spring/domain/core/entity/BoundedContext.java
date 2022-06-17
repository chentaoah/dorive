package com.gitee.spring.domain.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends LinkedHashMap<String, Object> {
    private Map<String, List<Object>> entitiesCache = new LinkedHashMap<>();
}
