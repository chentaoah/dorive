/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.agent.v1.impl.interceptor;

import com.gitee.dorive.module.agent.v1.impl.SpringApplicationAgent;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class SpringBootTestContextBootstrapperInterceptor {

    public static final String CLASS_NAME = "com.gitee.dorive.module.test.v1.impl.SpringBootModularContextLoader";

    @RuntimeType
    public static Object intercept(@Argument(0) Class<?> testClass) throws Exception {
        System.out.printf("[Test Agent] Intercepting method. Test class: %s%n", testClass.getName());

        ClassLoader classLoader = SpringApplicationAgent.getClassLoader(testClass);
        return Class.forName(CLASS_NAME, true, classLoader);
    }

}
