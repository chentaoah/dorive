/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
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
