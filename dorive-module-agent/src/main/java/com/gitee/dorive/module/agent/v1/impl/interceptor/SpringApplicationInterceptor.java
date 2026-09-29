/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.agent.v1.impl.interceptor;

import com.gitee.dorive.module.v1.impl.SpringModularApplication;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.Arrays;

public class SpringApplicationInterceptor {

    @RuntimeType
    public static Object intercept(@Argument(0) Class<?> primarySource, @Argument(1) String[] args) {
        System.out.printf("[Agent] Intercepting method. Primary source: %s, args: %s%n", primarySource.getName(), Arrays.toString(args));
        return SpringModularApplication.run(primarySource, args);
    }

}
