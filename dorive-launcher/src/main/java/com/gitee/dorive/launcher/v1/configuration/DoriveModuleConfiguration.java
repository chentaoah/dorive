/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.launcher.v1.configuration;

import com.gitee.dorive.module.v1.impl.environment.ModuleRequestMappingHandlerMapping;
import com.gitee.dorive.module.v1.impl.inject.ModuleAutowiredBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Order(-100)
@Configuration
@ConditionalOnProperty(prefix = "dorive.module", name = "enable", havingValue = "true")
public class DoriveModuleConfiguration {

    @Bean("moduleAutowiredBeanPostProcessorV3")
    @ConditionalOnMissingClass
    public static ModuleAutowiredBeanPostProcessor moduleAutowiredBeanPostProcessor() {
        return new ModuleAutowiredBeanPostProcessor();
    }

    @Bean("moduleWebMvcRegistrationsV3")
    @ConditionalOnMissingClass
    public static WebMvcRegistrations moduleWebMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new ModuleRequestMappingHandlerMapping();
            }
        };
    }

}
