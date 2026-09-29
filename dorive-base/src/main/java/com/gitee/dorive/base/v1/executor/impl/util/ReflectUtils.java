/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReflectUtils {

    public static List<Class<?>> getAllSuperclasses(Class<?> type, Class<?> ignoredType) {
        List<Class<?>> superclasses = new ArrayList<>();
        Class<?> superclass = type.getSuperclass();
        while (superclass != null) {
            if (superclass != ignoredType) {
                superclasses.add(superclass);
            }
            superclass = superclass.getSuperclass();
        }
        Collections.reverse(superclasses);
        return superclasses;
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Class<?>> classes = getAllSuperclasses(type, Object.class);
        classes.add(type);
        List<Field> fields = new ArrayList<>();
        for (Class<?> clazz : classes) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields;
    }

    public static Class<?> getFirstTypeArgument(Class<?> type) {
        Type genericSuperclass = type.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        return (Class<?>) actualTypeArgument;
    }

}
