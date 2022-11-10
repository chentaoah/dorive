/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.spring.domain.core.util;

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
