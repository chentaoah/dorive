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
import com.gitee.dorive.module.api.BeanNameEditor;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Getter
@Setter
public class ModuleConfigurationBeanNameEditor implements BeanNameEditor {

    public static final String BEAN_ANNOTATION_HELPER_CLASS_NAME = "org.springframework.context.annotation.BeanAnnotationHelper";
    public static final String CONFIGURATION_CLASS_BEAN_DEFINITION_CLASS_NAME = "org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader$ConfigurationClassBeanDefinition";

    private static final Map<Method, String> BEAN_NAME_CACHE;

    static {
        Class<?> beanAnnotationHelperClass = ClassUtil.loadClass(BEAN_ANNOTATION_HELPER_CLASS_NAME);
        Field beanNameCacheField = ReflectUtil.getField(beanAnnotationHelperClass, "beanNameCache");
        Object staticFieldValue = ReflectUtil.getStaticFieldValue(beanNameCacheField);
        BEAN_NAME_CACHE = castValue(staticFieldValue);
    }

    // 该方法是为了避免编译时提示使用了不安全的操作
    @SuppressWarnings("unchecked")
    public static <T> T castValue(Object value) {
        return (T) value;
    }

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;

    @Override
    public String reset(String beanName, BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        Class<?> beanDefinitionClass = beanDefinition.getClass();
        String className = beanDefinitionClass.getName();
        if (CONFIGURATION_CLASS_BEAN_DEFINITION_CLASS_NAME.equals(className)) {
            return resetConfigurationClassBeanDefinition(beanName, beanDefinition);
        }
        return beanName;
    }

    private String resetConfigurationClassBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        String factoryBeanName = beanDefinition.getFactoryBeanName();
        AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
        MethodMetadata factoryMethodMetadata = (MethodMetadata) ReflectUtil.getFieldValue(beanDefinition, "factoryMethodMetadata");
        String derivedBeanName = (String) ReflectUtil.getFieldValue(beanDefinition, "derivedBeanName");
        if (StringUtils.isNotBlank(factoryBeanName) && annotationMetadata != null && factoryMethodMetadata != null && StringUtils.isNotBlank(derivedBeanName)) {
            // 类型
            String className = annotationMetadata.getClassName();
            if (moduleParser.isUnderScanPackage(className)) {
                Class<?> configurationClass = ClassUtil.loadClass(className);
                // 方法
                String methodName = factoryMethodMetadata.getMethodName();
                Method factoryMethod = ReflectUtil.getMethod(configurationClass, methodName);
                // @Bean注解
                Map<String, Object> annotationAttributes = factoryMethodMetadata.getAnnotationAttributes(Bean.class.getName());
                if (annotationAttributes != null) {
                    Object name = annotationAttributes.get("name");
                    if (name instanceof String[] && ((String[]) name).length == 0) {
                        String newDerivedBeanName = factoryBeanName + "." + derivedBeanName;
                        BEAN_NAME_CACHE.put(factoryMethod, newDerivedBeanName);
                        ReflectUtil.setFieldValue(beanDefinition, "derivedBeanName", newDerivedBeanName);
                        return newDerivedBeanName;
                    }
                }
            }
        }
        return beanName;
    }

}
