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

import com.gitee.dorive.module.agent.v1.impl.interceptor.SpringApplicationInterceptor;
import com.gitee.dorive.module.agent.v1.impl.interceptor.SpringBootTestContextBootstrapperInterceptor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class SpringApplicationAgent {

    public static final String CLASS_NAME = "org.springframework.boot.SpringApplication";
    public static final String METHOD_NAME = "run";

    public static final String TEST_CLASS_NAME = "org.springframework.boot.test.context.SpringBootTestContextBootstrapper";
    public static final String TEST_METHOD_NAME = "getDefaultContextLoaderClass";

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.printf("[Agent] Starting agent. Agent args: %s, class name: %s, method name: %s%n", agentArgs, CLASS_NAME, METHOD_NAME);
        new AgentBuilder.Default()
                .type(named(CLASS_NAME))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(named(METHOD_NAME).and(takesArguments(Class.class, String[].class)).and(isPublic()).and(isStatic()))
                                .intercept(MethodDelegation.to(SpringApplicationInterceptor.class)))
                .installOn(inst);

        System.out.printf("[Test Agent] Starting agent. Agent args: %s, class name: %s, method name: %s%n", agentArgs, TEST_CLASS_NAME, TEST_METHOD_NAME);
        new AgentBuilder.Default()
                .type(named(TEST_CLASS_NAME))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                        builder.method(named(TEST_METHOD_NAME).and(takesArguments(Class.class)))
                                .intercept(MethodDelegation.to(SpringBootTestContextBootstrapperInterceptor.class)))
                .installOn(inst);
    }

}
