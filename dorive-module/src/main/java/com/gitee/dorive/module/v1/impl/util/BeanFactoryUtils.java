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

package com.gitee.dorive.module.v1.impl.util;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

public class BeanFactoryUtils {

    public static Class<?> tryGetConfigurationClass(DefaultListableBeanFactory beanFactory, Class<?> beanType, Object bean) {
        // class of factory bean
        BeanDefinition beanDefinition = BeanFactoryUtils.getBeanDefinition(beanFactory, beanType, bean);
        if (SpringClassUtils.isConfigurationBeanDefinition(beanDefinition)) {
            AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
            String className = annotationMetadata.getClassName();
            return ClassUtil.loadClass(className);
        }
        return null;
    }

    public static BeanDefinition getBeanDefinition(DefaultListableBeanFactory beanFactory, Class<?> beanType, Object bean) {
        String[] beanNamesForType = beanFactory.getBeanNamesForType(beanType);
        if (beanNamesForType.length == 1) {
            return beanFactory.getBeanDefinition(beanNamesForType[0]);
        }
        for (String beanName : beanNamesForType) {
            Object candidateBean = beanFactory.getBean(beanName);
            if (bean == candidateBean) {
                return beanFactory.getBeanDefinition(beanName);
            }
        }
        return null;
    }

}
