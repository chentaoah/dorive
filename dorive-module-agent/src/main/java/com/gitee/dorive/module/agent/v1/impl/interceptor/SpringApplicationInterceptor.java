/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.agent.v1.impl.interceptor;

import com.gitee.dorive.module.agent.v1.impl.SpringApplicationAgent;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.Arrays;

public class SpringApplicationInterceptor {

    public static final String CLASS_NAME = "com.gitee.dorive.module.v1.impl.SpringModularApplication";
    public static final String METHOD_NAME = "run";

    @RuntimeType
    public static Object intercept(@Argument(0) Class<?> primarySource, @Argument(1) String[] args) throws Exception {
        System.out.printf("[Agent] Intercepting method. Primary source: %s, args: %s%n", primarySource.getName(), Arrays.toString(args));

        ClassLoader classLoader = SpringApplicationAgent.getClassLoader(primarySource);
        Class<?> clazz = Class.forName(CLASS_NAME, true, classLoader);
        Method method = clazz.getMethod(METHOD_NAME, Class.class, String[].class);
        return method.invoke(null, primarySource, args);
    }

}
