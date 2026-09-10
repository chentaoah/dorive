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
