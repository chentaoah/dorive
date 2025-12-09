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

package com.gitee.dorive.module.impl.util;

import cn.hutool.core.util.ClassUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;

import java.lang.reflect.InvocationHandler;

public class SpringClassUtils {
    public static final String BEAN_ANNOTATION_HELPER_NAME = "org.springframework.context.annotation.BeanAnnotationHelper";
    public static final String CONFIGURATION_CLASS_BEAN_DEFINITION_NAME = "org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader$ConfigurationClassBeanDefinition";
    public static final String STREAM_DEPENDENCY_DESCRIPTOR_NAME = "org.springframework.beans.factory.support.DefaultListableBeanFactory$StreamDependencyDescriptor";
    public static final String MULTI_ELEMENT_DESCRIPTOR_NAME = "org.springframework.beans.factory.support.DefaultListableBeanFactory$MultiElementDescriptor";
    public static final String SYNTHESIZED_MERGED_ANNOTATION_INVOCATION_HANDLER_NAME = "org.springframework.core.annotation.SynthesizedMergedAnnotationInvocationHandler";

    public static final Class<?> BEAN_ANNOTATION_HELPER = ClassUtil.loadClass(BEAN_ANNOTATION_HELPER_NAME);
    public static final Class<?> CONFIGURATION_CLASS_BEAN_DEFINITION = ClassUtil.loadClass(CONFIGURATION_CLASS_BEAN_DEFINITION_NAME);
    public static final Class<?> STREAM_DEPENDENCY_DESCRIPTOR = ClassUtil.loadClass(STREAM_DEPENDENCY_DESCRIPTOR_NAME);
    public static final Class<?> MULTI_ELEMENT_DESCRIPTOR = ClassUtil.loadClass(MULTI_ELEMENT_DESCRIPTOR_NAME);
    public static final Class<?> SYNTHESIZED_MERGED_ANNOTATION_INVOCATION_HANDLER = ClassUtil.loadClass(SYNTHESIZED_MERGED_ANNOTATION_INVOCATION_HANDLER_NAME);

    public static boolean isConfigurationBeanDefinition(BeanDefinition beanDefinition) {
        return beanDefinition != null && beanDefinition.getClass() == CONFIGURATION_CLASS_BEAN_DEFINITION;
    }

    public static boolean isStreamDependencyDescriptor(DependencyDescriptor descriptor) {
        return descriptor != null && descriptor.getClass() == STREAM_DEPENDENCY_DESCRIPTOR;
    }

    public static boolean isMultiElementDescriptor(DependencyDescriptor descriptor) {
        return descriptor != null && descriptor.getClass() == MULTI_ELEMENT_DESCRIPTOR;
    }

    public static boolean isSynthesizedMergedAnnotationInvocationHandler(InvocationHandler invocationHandler) {
        return invocationHandler != null && invocationHandler.getClass() == SYNTHESIZED_MERGED_ANNOTATION_INVOCATION_HANDLER;
    }
}
