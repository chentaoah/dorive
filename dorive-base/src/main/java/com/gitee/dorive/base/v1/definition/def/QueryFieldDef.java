/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.def;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.definition.annotation.QueryField;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@Data
public class QueryFieldDef {
    private String[] path;
    private Class<?> entity;
    private String name;
    private String field;
    private String operator;

    public static QueryFieldDef fromElement(AnnotatedElement element) {
        Map<String, Object> attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(element, QueryField.class);
        return attributes != null ? BeanUtil.copyProperties(attributes, QueryFieldDef.class) : null;
    }
}
