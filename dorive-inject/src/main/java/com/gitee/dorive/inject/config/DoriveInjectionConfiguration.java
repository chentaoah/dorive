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

package com.gitee.dorive.inject.config;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.inject.api.ModuleChecker;
import com.gitee.dorive.inject.entity.ModuleDefinition;
import com.gitee.dorive.inject.impl.DefaultModuleChecker;
import com.gitee.dorive.inject.spring.LimitedAutowiredBeanPostProcessor;
import com.gitee.dorive.inject.spring.LimitedCglibSubclassingInstantiationStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.Comparator;
import java.util.List;

@Order(-100)
@Configuration
@ConditionalOnProperty(prefix = "dorive", name = "enable", havingValue = "true")
public class DoriveInjectionConfiguration implements EnvironmentAware, BeanFactoryPostProcessor {

    public static final String DORIVE_SCAN_KEY = "dorive.scan";
    public static final String DORIVE_MODULES_KEY = "dorive.modules";

    public static ModuleChecker moduleChecker;

    @Override
    public void setEnvironment(Environment environment) {
        String scanPackage = environment.getProperty(DORIVE_SCAN_KEY);
        Assert.notBlank(scanPackage, "The configuration item could not be found! name: {}", DORIVE_SCAN_KEY);
        List<ModuleDefinition> moduleDefinitions = Binder.get(environment).bind(DORIVE_MODULES_KEY, Bindable.listOf(ModuleDefinition.class)).get();
        moduleDefinitions.sort(Comparator.comparing(ModuleDefinition::getName));
        moduleChecker = new DefaultModuleChecker(scanPackage, moduleDefinitions);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (moduleChecker != null && beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory abstractBeanFactory = (AbstractAutowireCapableBeanFactory) beanFactory;
            abstractBeanFactory.setInstantiationStrategy(new LimitedCglibSubclassingInstantiationStrategy(moduleChecker));
        }
    }

    @Bean("limitedAnnotationBeanPostProcessorV3")
    @ConditionalOnMissingClass
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor() {
        return new LimitedAutowiredBeanPostProcessor(moduleChecker);
    }

}
