/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
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
                // noinspection ConstantConditions
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
