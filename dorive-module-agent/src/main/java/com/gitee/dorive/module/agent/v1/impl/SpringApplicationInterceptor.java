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

package com.gitee.dorive.module.agent.v1.impl;

import com.gitee.dorive.module.v1.impl.SpringModularApplication;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.util.Arrays;

public class SpringApplicationInterceptor {

    @RuntimeType
    public static Object intercept(@Argument(0) Class<?> primarySource, @Argument(1) String[] args) {
        System.out.printf("[Agent] Intercepting method. Primary source: %s, args: %s%n", primarySource.getName(), Arrays.toString(args));
        // 完全替换原有的启动逻辑，不调用 originalCall.call()
        return SpringModularApplication.run(primarySource, args);
    }

}
