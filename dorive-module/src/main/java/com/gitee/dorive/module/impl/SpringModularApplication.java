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

package com.gitee.dorive.module.impl;

import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.impl.spring.ModuleAnnotationBeanNameGenerator;
import com.gitee.dorive.module.impl.spring.ModuleConfigurationClassPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;

import java.util.*;

import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME;

public class SpringModularApplication extends SpringApplication {

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
        moduleParser.parse();
        List<ModuleDefinition> moduleDefinitions = moduleParser.getModuleDefinitions();

        Set<Class<?>> sources = new LinkedHashSet<>();
        Set<String> profiles = new LinkedHashSet<>();
        for (ModuleDefinition moduleDefinition : moduleDefinitions) {
            Class<?> mainClass = moduleDefinition.getMainClass();
            if (mainClass != null) {
                sources.add(mainClass);
            }
            profiles.addAll(moduleDefinition.getProfiles());
        }
        sources.add(primarySource);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("dorive.module.enable", true);
        BeanNameGenerator beanNameGenerator = new ModuleAnnotationBeanNameGenerator();

        return new SpringModularApplicationBuilder(sources.toArray(new Class[0]))
                .profiles(profiles.toArray(new String[0]))
                .properties(properties)
                .beanNameGenerator(beanNameGenerator)
                .run(args);
    }

    public SpringModularApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        super(resourceLoader, primarySources);
    }

    @Override
    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        super.postProcessApplicationContext(context);
        if (context instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;
            String beanName = CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME;
            if (registry.containsBeanDefinition(beanName)) {
                registry.removeBeanDefinition(beanName);
                RootBeanDefinition beanDefinition = new RootBeanDefinition(ModuleConfigurationClassPostProcessor.class);
                beanDefinition.setSource(null);
                beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                registry.registerBeanDefinition(beanName, beanDefinition);
            }
        }
    }

}
