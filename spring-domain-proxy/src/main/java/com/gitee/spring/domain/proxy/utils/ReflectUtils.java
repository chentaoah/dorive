package com.gitee.spring.domain.proxy.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public class ReflectUtils {

    public static Type getGenericSuperclass(Object object, Class<?> targetClass) {
        return object.getClass().getGenericSuperclass();
    }

    public static Constructor<?> getConstructor(Class<?> type, Class<?>[] parameterTypes) {
        return org.springframework.cglib.core.ReflectUtils.getConstructor(type, parameterTypes);
    }

    public static Object newInstance(Constructor<?> constructor, Object[] args) {
        return org.springframework.cglib.core.ReflectUtils.newInstance(constructor, args);
    }

}
