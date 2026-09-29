/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.factory;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class ModuleApplicationContextFactory implements ApplicationContextFactory {

    @Override
    public Class<? extends ConfigurableEnvironment> getEnvironmentType(WebApplicationType webApplicationType) {
        return ApplicationContextFactory.DEFAULT.getEnvironmentType(webApplicationType);
    }

    @Override
    public ConfigurableEnvironment createEnvironment(WebApplicationType webApplicationType) {
        return ApplicationContextFactory.DEFAULT.createEnvironment(webApplicationType);
    }

    @Override
    public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
        try {
            if (webApplicationType == WebApplicationType.REACTIVE) {
                return new AnnotationConfigReactiveWebServerApplicationContext(new ModuleDefaultListableBeanFactory());
            }
            if (webApplicationType == WebApplicationType.SERVLET) {
                return new AnnotationConfigServletWebServerApplicationContext(new ModuleDefaultListableBeanFactory());
            }
            return ApplicationContextFactory.DEFAULT.create(webApplicationType);

        } catch (Exception ex) {
            throw new IllegalStateException("Unable create a default ApplicationContext instance, "
                    + "you may need a custom ApplicationContextFactory", ex);
        }
    }

}
