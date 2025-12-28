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

package com.gitee.dorive.base.v1.core.util;

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
