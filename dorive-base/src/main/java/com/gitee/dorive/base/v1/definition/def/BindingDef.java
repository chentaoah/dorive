/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.def;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.definition.annotation.Binding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BindingDef {
    private String source;
    private String literal;
    private String target;
    private String expression;
    private Class<?> processor;
    private String targetField;

    public static List<BindingDef> fromElement(AnnotatedElement element) {
        Set<Binding> bindingAnnotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(element, Binding.class);
        List<BindingDef> bindingDefs = new ArrayList<>(bindingAnnotations.size());
        for (Binding bindingAnnotation : bindingAnnotations) {
            Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation);
            bindingDefs.add(BeanUtil.copyProperties(attributes, BindingDef.class));
        }
        return bindingDefs;
    }
}
