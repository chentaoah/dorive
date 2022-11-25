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
package com.gitee.dorive.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ReflectUtils {

    public static Class<?> getFirstArgumentType(Class<?> type) {
        Type genericSuperclass = type.getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        return (Class<?>) actualTypeArgument;
    }

    public static Object newInstance(Class<?> type) {
        return org.springframework.cglib.core.ReflectUtils.newInstance(type);
    }

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

    public static Set<String> getFieldNames(Class<?> type) {
        Set<String> fieldNames = new LinkedHashSet<>();
        for (Field field : type.getDeclaredFields()) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

}
