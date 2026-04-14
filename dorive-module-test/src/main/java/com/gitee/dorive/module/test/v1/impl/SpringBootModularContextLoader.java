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

package com.gitee.dorive.module.test.v1.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.module.v1.impl.SpringModularApplication;
import com.gitee.dorive.module.v1.impl.factory.ModuleDefaultListableBeanFactory;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTestAnnotationProxy;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class SpringBootModularContextLoader extends SpringBootContextLoader {

    private Class<?> primarySource;
    private String[] args;

    @Override
    public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        Class<?>[] configClasses = mergedConfig.getClasses();
        if (configClasses.length > 0) {
            for (Class<?> configClass : configClasses) {
                SpringBootApplication annotation = AnnotationUtils.getAnnotation(configClass, SpringBootApplication.class);
                //noinspection ConstantConditions
                if (annotation != null) {
                    this.primarySource = configClass;
                    break;
                }
            }
        }
        this.args = SpringBootTestAnnotationProxy.get(mergedConfig);
        return super.loadContext(mergedConfig);
    }

    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplicationBuilder builder = SpringModularApplication.build(primarySource, args);
        return builder.build();
    }

    @Override
    protected ApplicationContextFactory getApplicationContextFactory(MergedContextConfiguration mergedConfig) {
        boolean isEmbeddedWebEnvironment = ReflectUtil.invoke(this, "isEmbeddedWebEnvironment", mergedConfig);
        return (webApplicationType) -> {
            if (webApplicationType != WebApplicationType.NONE && !isEmbeddedWebEnvironment) {
                if (webApplicationType == WebApplicationType.REACTIVE) {
                    return new GenericReactiveWebApplicationContext(new ModuleDefaultListableBeanFactory());
                }
                if (webApplicationType == WebApplicationType.SERVLET) {
                    return new GenericWebApplicationContext(new ModuleDefaultListableBeanFactory());
                }
            }
            return ApplicationContextFactory.DEFAULT.create(webApplicationType);
        };
    }

}
