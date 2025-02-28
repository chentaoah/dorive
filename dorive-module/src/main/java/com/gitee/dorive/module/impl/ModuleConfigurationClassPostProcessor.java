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

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.inject.api.ModuleChecker;
import com.gitee.dorive.inject.config.DoriveInjectionConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class ModuleConfigurationClassPostProcessor extends ConfigurationClassPostProcessor implements MethodInterceptor {

    public static final String BEAN_DEFINITION_CLASS_NAME = "org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader$ConfigurationClassBeanDefinition";
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        if (registry instanceof DefaultListableBeanFactory) {
            this.beanFactory = (DefaultListableBeanFactory) registry;
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(DefaultListableBeanFactory.class);
            enhancer.setCallback(this);
            registry = (BeanDefinitionRegistry) enhancer.create();
        }
        super.postProcessBeanDefinitionRegistry(registry);
    }

    @Override
    public Object intercept(Object instance, Method method, Object[] args, MethodProxy methodProxy) {
        String methodName = method.getName();
        if ("registerBeanDefinition".equals(methodName)) {
            BeanDefinition beanDefinition = (BeanDefinition) args[1];
            Class<? extends BeanDefinition> beanDefinitionClass = beanDefinition.getClass();
            String className = beanDefinitionClass.getName();
            if (BEAN_DEFINITION_CLASS_NAME.equals(className)) {
                String factoryBeanName = beanDefinition.getFactoryBeanName();
                String factoryMethodName = beanDefinition.getFactoryMethodName();
                Field field = ReflectUtil.getField(beanDefinitionClass, "derivedBeanName");
                if (isUnderScanPackage(factoryBeanName) && StringUtils.isNotBlank(factoryMethodName) && field != null) {
                    Object derivedBeanName = ReflectUtil.getFieldValue(beanDefinition, field);
                    if (Objects.equals(factoryMethodName, derivedBeanName)) {
                        derivedBeanName = factoryBeanName + "." + derivedBeanName;
                        args[0] = derivedBeanName;
                        ReflectUtil.setFieldValue(beanDefinition, field, derivedBeanName);
                    }
                }
            }
        }
        return ReflectUtil.invoke(beanFactory, method, args);
    }

    private boolean isUnderScanPackage(String factoryBeanName) {
        ModuleChecker moduleChecker = DoriveInjectionConfiguration.moduleChecker;
        return moduleChecker != null && moduleChecker.isNotSpringInternalType(factoryBeanName) && moduleChecker.isUnderScanPackage(factoryBeanName);
    }

}
