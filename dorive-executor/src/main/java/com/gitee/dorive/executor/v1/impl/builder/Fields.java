package com.gitee.dorive.executor.v1.impl.builder;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Fields {

    public static List<String> getFieldNames(Class<?> type, boolean withSuper, String... ignoreFields) {
        Field[] fields = ReflectUtil.getFieldsDirectly(type, withSuper);
        Assert.notEmpty(fields, "The fields cannot be empty!");
        Set<String> ignoreFieldsSet = Arrays.stream(ignoreFields).collect(Collectors.toSet());
        return Arrays.stream(fields) //
                .filter(f -> !Modifier.isStatic(f.getModifiers())) //
                .map(Field::getName) //
                .filter(fn -> !ignoreFieldsSet.contains(fn)) //
                .toList();
    }

    public static String of(Class<?> type, boolean withSuper, String... ignoreFields) {
        List<String> fieldNames = getFieldNames(type, withSuper, ignoreFields);
        return String.join(", ", fieldNames);
    }

    public static String of(Class<?> type, String... ignoreFields) {
        return of(type, false, ignoreFields);
    }

}
