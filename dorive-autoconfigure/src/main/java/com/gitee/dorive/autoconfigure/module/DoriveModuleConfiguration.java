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

package com.gitee.dorive.autoconfigure.module;

import com.gitee.dorive.module.impl.filter.BoundedContextExposedBeanFilter;
import com.gitee.dorive.module.impl.inject.BoundedContextBeanPostProcessor;
import com.gitee.dorive.module.impl.inject.ModuleAutowiredBeanPostProcessor;
import com.gitee.dorive.module.impl.environment.ModuleRequestMappingHandlerMapping;
import com.gitee.dorive.module.impl.runner.ModuleApplicationRunner;
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

    @Bean("boundedContextExposedBeanFilterV3")
    @ConditionalOnMissingClass
    public static BoundedContextExposedBeanFilter boundedContextExposedBeanFilter() {
        return new BoundedContextExposedBeanFilter();
    }

    @Bean("boundedContextBeanPostProcessorV3")
    @ConditionalOnMissingClass
    public static BoundedContextBeanPostProcessor boundedContextBeanPostProcessor() {
        return new BoundedContextBeanPostProcessor();
    }

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

    @Bean("moduleApplicationRunnerV3")
    @ConditionalOnMissingClass
    public static ModuleApplicationRunner moduleApplicationRunner() {
        return new ModuleApplicationRunner();
    }

}
