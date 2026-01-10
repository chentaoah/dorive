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

package com.gitee.dorive.module.v1.impl.util;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

public class ClassLoaderUtils {

    public static void loadUrls(URLClassLoader classLoader, Set<URL> urls) {
        try {
            final Method method = ClassUtil.getDeclaredMethod(URLClassLoader.class, "addURL", URL.class);
            if (method != null) {
                method.setAccessible(true);
                for (URL url : urls) {
                    ReflectUtil.invoke(classLoader, method, url);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
