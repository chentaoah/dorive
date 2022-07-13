package com.gitee.spring.domain.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;

@Data
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends LinkedHashMap<String, Object> {
}
