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
package com.gitee.dorive.injection.config;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.injection.impl.DefaultTypeDomainResolver;
import com.gitee.dorive.injection.spring.LimitedAutowiredBeanPostProcessor;
import com.gitee.dorive.injection.spring.LimitedCglibSubclassingInstantiationStrategy;
import com.gitee.dorive.injection.spring.LimitedRootInitializingBean;
import com.gitee.dorive.injection.api.TypeDomainResolver;
import com.gitee.dorive.injection.entity.DomainDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;

@Order(-100)
@Configuration
@ConditionalOnProperty(prefix = "dorive", name = "enable", havingValue = "true")
public class DomainInjectionConfiguration implements BeanFactoryPostProcessor {

    public static final String SCAN_KEY = "dorive.scan";
    public static final String DOMAINS_KEY = "dorive.domains";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            AbstractAutowireCapableBeanFactory abstractAutowireCapableBeanFactory = (AbstractAutowireCapableBeanFactory) beanFactory;
            abstractAutowireCapableBeanFactory.setInstantiationStrategy(new LimitedCglibSubclassingInstantiationStrategy());
        }
    }

    @Bean("typeDomainResolverV3")
    @ConditionalOnMissingClass
    public TypeDomainResolver typeDomainResolver(Environment environment) {
        String scanPackage = environment.getProperty(SCAN_KEY);
        Assert.notBlank(scanPackage, "The configuration item could not be found! name: [spring.domain.scan]");
        List<DomainDefinition> domainDefinitions = Binder.get(environment).bind(DOMAINS_KEY, Bindable.listOf(DomainDefinition.class)).get();
        domainDefinitions.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
        return new DefaultTypeDomainResolver(scanPackage, domainDefinitions);
    }

    @Bean("limitedAnnotationBeanPostProcessorV3")
    @ConditionalOnMissingClass
    public LimitedAutowiredBeanPostProcessor limitedAnnotationBeanPostProcessor(TypeDomainResolver typeDomainResolver) {
        return new LimitedAutowiredBeanPostProcessor(typeDomainResolver);
    }

    @Bean("limitedRootInitializingBeanV3")
    @ConditionalOnMissingClass
    public LimitedRootInitializingBean limitedRootInitializingBean(TypeDomainResolver typeDomainResolver) {
        return new LimitedRootInitializingBean(typeDomainResolver);
    }

}
