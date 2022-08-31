package com.gitee.spring.domain.core.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReflectUtils {

    public static Constructor<?> getConstructor(Class<?> type, Class<?>[] parameterTypes) {
        return org.springframework.cglib.core.ReflectUtils.getConstructor(type, parameterTypes);
    }

    public static Object newInstance(Class<?> type) {
        return org.springframework.cglib.core.ReflectUtils.newInstance(type);
    }

    public static List<Class<?>> getAllSuperClasses(Class<?> type, Class<?> ignoredType) {
        List<Class<?>> superClasses = new ArrayList<>();
        Class<?> superClass = type.getSuperclass();
        while (superClass != null) {
            if (superClass != ignoredType) {
                superClasses.add(superClass);
            }
            superClass = superClass.getSuperclass();
        }
        Collections.reverse(superClasses);
        return superClasses;
    }

    public static Set<String> getFieldNames(Class<?> type) {
        Set<String> fieldNames = new LinkedHashSet<>();
        for (Field field : type.getDeclaredFields()) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

}
