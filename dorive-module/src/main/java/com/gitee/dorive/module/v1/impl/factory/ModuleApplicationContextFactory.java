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
