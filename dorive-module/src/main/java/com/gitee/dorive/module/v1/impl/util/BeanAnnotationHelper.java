/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.util;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class BeanAnnotationHelper {

    public static final Map<Method, String> BEAN_NAME_CACHE;

    static {
        Field beanNameCacheField = ReflectUtil.getField(SpringClassUtils.BEAN_ANNOTATION_HELPER, "beanNameCache");
        Object beanNameCacheFieldValue = ReflectUtil.getStaticFieldValue(beanNameCacheField);
        BEAN_NAME_CACHE = castValue(beanNameCacheFieldValue);
    }

    // 该方法是为了避免编译时提示使用了不安全的操作
    @SuppressWarnings("unchecked")
    public static <T> T castValue(Object value) {
        return (T) value;
    }

}
