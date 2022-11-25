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
package com.gitee.dorive.core.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.proxy.ProxyCompiler;
import com.gitee.dorive.proxy.JavassistCompiler;
import com.gitee.dorive.core.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PropertyProxyFactory {

    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final ProxyCompiler PROXY_COMPILER = new JavassistCompiler();
    private static final Map<String, PropertyProxy> GENERATED_PROXY_CACHE = new ConcurrentHashMap<>();

    public static PropertyProxy newPropertyProxy(Class<?> entityClass, Field declaredField) {
        return newPropertyProxy(entityClass, declaredField.getType(), declaredField.getName());
    }

    public static PropertyProxy newPropertyProxy(Class<?> entityClass, String fieldName) {
        try {
            Field field = ReflectUtil.getField(entityClass, fieldName);
            Class<?> fieldClass = field.getType();
            return newPropertyProxy(entityClass, fieldClass, fieldName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate class!", e);
        }
    }

    public static PropertyProxy newPropertyProxy(Class<?> entityClass, Class<?> fieldClass, String fieldName) {
        String cacheKey = entityClass.getTypeName() + ":" + fieldClass.getTypeName() + ":" + fieldName;
        if (!GENERATED_PROXY_CACHE.containsKey(cacheKey)) {
            synchronized (GENERATED_PROXY_CACHE) {
                if (!GENERATED_PROXY_CACHE.containsKey(cacheKey)) {
                    try {
                        String generatedCode = generateCode(entityClass, fieldClass, fieldName);
                        Class<?> generatedClass = PROXY_COMPILER.compile(generatedCode, null);
                        PropertyProxy propertyProxy = (PropertyProxy) ReflectUtils.newInstance(generatedClass);
                        GENERATED_PROXY_CACHE.put(cacheKey, propertyProxy);

                    } catch (Exception e) {
                        throw new RuntimeException("Failed to generate class!", e);
                    }
                }
            }
        }
        return GENERATED_PROXY_CACHE.get(cacheKey);
    }

    private static String generateCode(Class<?> entityClass, Class<?> fieldClass, String fieldName) {
        Class<?> interfaceClass = PropertyProxy.class;
        StringBuilder builder = new StringBuilder();
        String simpleName = interfaceClass.getSimpleName() + "$Proxy" + COUNT.getAndIncrement();
        builder.append(String.format("package %s;\n", interfaceClass.getPackage().getName()));
        builder.append(String.format("public class %s implements %s {\n", simpleName, interfaceClass.getName()));

        builder.append("\t").append(String.format("public %s getValue(%s arg0) {\n", Object.class.getTypeName(), Object.class.getTypeName()));
        builder.append("\t\t").append(String.format("%s arg1 = (%s)arg0;\n", entityClass.getTypeName(), entityClass.getTypeName()));
        builder.append("\t\t").append(String.format("return arg1.get%s();\n", StrUtil.upperFirst(fieldName)));
        builder.append("\t").append("}\n");

        builder.append("\t").append(String.format("public void setValue(%s arg0, %s arg1) {\n", Object.class.getTypeName(), Object.class.getTypeName()));
        builder.append("\t\t").append(String.format("%s arg2 = (%s)arg0;\n", entityClass.getTypeName(), entityClass.getTypeName()));
        builder.append("\t\t").append(String.format("%s arg3 = (%s)arg1;\n", fieldClass.getTypeName(), fieldClass.getTypeName()));
        builder.append("\t\t").append(String.format("arg2.set%s(arg3);\n", StrUtil.upperFirst(fieldName)));
        builder.append("\t").append("}\n");

        builder.append("}\n");
        return builder.toString();
    }

}
