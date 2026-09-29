/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.agent.v1.impl.interceptor;

import com.gitee.dorive.module.test.v1.impl.SpringBootModularContextLoader;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public class SpringBootTestContextBootstrapperInterceptor {

    @RuntimeType
    public static Object intercept(@Argument(0) Class<?> testClass) {
        System.out.printf("[Test Agent] Intercepting method. Test class: %s%n", testClass.getName());
        return SpringBootModularContextLoader.class;
    }

}
