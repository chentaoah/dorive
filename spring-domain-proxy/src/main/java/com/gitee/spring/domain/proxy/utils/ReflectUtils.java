package com.gitee.spring.domain.proxy.utils;

import cn.hutool.core.lang.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Objects;

public class ReflectUtils {

    public static Type getGenericSuperclass(Object object, Class<?> targetClass) {
        Class<?> clazz = object.getClass();
        while (clazz != null && clazz != Object.class && clazz != targetClass) {
            clazz = clazz.getSuperclass();
        }
        Assert.isTrue(clazz != null && clazz != Object.class, "Failed to get superclass of target type!");
        return Objects.requireNonNull(clazz).getGenericSuperclass();
    }

    public static Constructor<?> getConstructor(Class<?> type, Class<?>[] parameterTypes) {
        return org.springframework.cglib.core.ReflectUtils.getConstructor(type, parameterTypes);
    }

    public static Object newInstance(Constructor<?> constructor, Object[] args) {
        return org.springframework.cglib.core.ReflectUtils.newInstance(constructor, args);
    }

    public static Object newInstance(Class<?> type) {
        return org.springframework.cglib.core.ReflectUtils.newInstance(type);
    }

}
