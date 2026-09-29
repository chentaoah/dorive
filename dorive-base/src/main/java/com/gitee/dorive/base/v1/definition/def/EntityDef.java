/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.def;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.definition.annotation.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDef {
    private String name;
    private boolean aggregate;
    private Class<?> repository;
    private int priority;

    public static EntityDef fromElement(AnnotatedElement element) {
        Map<String, Object> attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(element, Entity.class);
        return attributes != null ? BeanUtil.copyProperties(attributes, EntityDef.class) : null;
    }
}
