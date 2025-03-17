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

package com.gitee.dorive.module.impl.uitl;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class BeanAnnotationHelper {

    public static final String BEAN_ANNOTATION_HELPER_CLASS_NAME = "org.springframework.context.annotation.BeanAnnotationHelper";
    public static final Map<Method, String> BEAN_NAME_CACHE;

    static {
        Class<?> beanAnnotationHelperClass = ClassUtil.loadClass(BeanAnnotationHelper.BEAN_ANNOTATION_HELPER_CLASS_NAME);
        Field beanNameCacheField = ReflectUtil.getField(beanAnnotationHelperClass, "beanNameCache");
        Object beanNameCacheFieldValue = ReflectUtil.getStaticFieldValue(beanNameCacheField);
        BEAN_NAME_CACHE = castValue(beanNameCacheFieldValue);
    }

    // 该方法是为了避免编译时提示使用了不安全的操作
    @SuppressWarnings("unchecked")
    public static <T> T castValue(Object value) {
        return (T) value;
    }

}
