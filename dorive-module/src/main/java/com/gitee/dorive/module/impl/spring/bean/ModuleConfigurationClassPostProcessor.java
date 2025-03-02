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

package com.gitee.dorive.module.impl.spring.bean;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.type.MethodMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Getter
@Setter
public class ModuleConfigurationClassPostProcessor extends ConfigurationClassPostProcessor implements MethodInterceptor {

    public static final String CONFIGURATION_CLASS_BEAN_DEFINITION_CLASS_NAME = "org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader$ConfigurationClassBeanDefinition";
    public static final String BEAN_ANNOTATION_HELPER_CLASS_NAME = "org.springframework.context.annotation.BeanAnnotationHelper";

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
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
            if (CONFIGURATION_CLASS_BEAN_DEFINITION_CLASS_NAME.equals(className)) {
                String factoryBeanName = beanDefinition.getFactoryBeanName();
                if (factoryBeanName != null && moduleParser.isUnderScanPackage(factoryBeanName)) {
                    MethodMetadata factoryMethodMetadata = (MethodMetadata) ReflectUtil.getFieldValue(beanDefinition, "factoryMethodMetadata");
                    String derivedBeanName = (String) ReflectUtil.getFieldValue(beanDefinition, "derivedBeanName");
                    if (factoryMethodMetadata != null && StringUtils.isNotBlank(derivedBeanName)) {
                        resetBeanName(args, beanDefinition, factoryBeanName, factoryMethodMetadata, derivedBeanName);
                    }
                }
            }
        }
        return ReflectUtil.invoke(beanFactory, method, args);
    }

    @SuppressWarnings("unchecked")
    private void resetBeanName(Object[] args, BeanDefinition beanDefinition, String factoryBeanName, MethodMetadata factoryMethodMetadata, String derivedBeanName) {
        Map<String, Object> annotationAttributes = factoryMethodMetadata.getAnnotationAttributes(Bean.class.getName());
        if (annotationAttributes != null) {
            Object name = annotationAttributes.get("name");
            if (name instanceof String[] && ((String[]) name).length == 0) {
                Class<?> beanAnnotationHelperClass = ClassUtil.loadClass(BEAN_ANNOTATION_HELPER_CLASS_NAME);
                Field beanNameCacheField = ReflectUtil.getField(beanAnnotationHelperClass, "beanNameCache");
                Map<Method, String> beanNameCache = (Map<Method, String>) ReflectUtil.getStaticFieldValue(beanNameCacheField);

                Class<?> beanClass = ClassUtil.loadClass(factoryBeanName);
                String factoryMethodName = beanDefinition.getFactoryMethodName();
                Method factoryMethod = ReflectUtil.getMethod(beanClass, factoryMethodName);

                derivedBeanName = factoryBeanName + "." + derivedBeanName;
                beanNameCache.put(factoryMethod, derivedBeanName);
                args[0] = derivedBeanName;
                ReflectUtil.setFieldValue(beanDefinition, "derivedBeanName", derivedBeanName);
            }
        }
    }

}
