package com.gitee.spring.domain.core3.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends LinkedHashMap<String, Object> {
}
