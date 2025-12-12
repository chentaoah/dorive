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

package com.gitee.dorive.test.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.module.v1.impl.SpringModularApplication;
import com.gitee.dorive.module.v1.impl.factory.ModuleDefaultListableBeanFactory;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.lang.reflect.Method;

public class SpringBootModularContextLoader extends SpringBootContextLoader implements MethodInterceptor {

    private MergedContextConfiguration config;
    private Class<?> primarySource;
    private SpringApplication springApplication;

    @Override
    public ApplicationContext loadContext(MergedContextConfiguration config) throws Exception {
        this.config = config;
        Class<?>[] configClasses = config.getClasses();
        if (configClasses.length > 0) {
            for (Class<?> configClass : configClasses) {
                SpringBootApplication annotation = AnnotationUtils.getAnnotation(configClass, SpringBootApplication.class);
                if (annotation != null) {
                    this.primarySource = configClass;
                    break;
                }
            }
        }
        return super.loadContext(config);
    }

    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplicationBuilder builder = SpringModularApplication.build(primarySource);
        this.springApplication = builder.build();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SpringApplication.class);
        enhancer.setCallback(this);
        Class<?>[] argumentTypes = new Class[]{Class[].class};
        Object[] arguments = new Object[]{new Class[]{primarySource}};
        return (SpringApplication) enhancer.create(argumentTypes, arguments);
    }

    @Override
    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy) throws Exception {
        String methodName = method.getName();
        if ("setApplicationContextFactory".equals(methodName)) {
            boolean isEmbeddedWebEnvironment = ReflectUtil.invoke(this, "isEmbeddedWebEnvironment", config);
            if (!isEmbeddedWebEnvironment) {
                springApplication.setApplicationContextFactory((type) -> {
                    if (type != WebApplicationType.NONE) {
                        if (type == WebApplicationType.REACTIVE) {
                            return new GenericReactiveWebApplicationContext(new ModuleDefaultListableBeanFactory());

                        } else if (type == WebApplicationType.SERVLET) {
                            return new GenericWebApplicationContext(new ModuleDefaultListableBeanFactory());
                        }
                    }
                    return ApplicationContextFactory.DEFAULT.create(type);
                });
            }
            return null;
        }
        return method.invoke(springApplication, args);
    }

}
