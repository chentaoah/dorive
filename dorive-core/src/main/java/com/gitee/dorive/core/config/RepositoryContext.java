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

package com.gitee.dorive.core.config;

import cn.hutool.core.util.ClassUtil;
import com.gitee.dorive.api.util.ReflectUtils;
import com.gitee.dorive.core.api.common.RepositoryPostProcessor;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RepositoryContext implements BeanFactoryPostProcessor {

    private static final Map<Class<?>, Class<?>> ENTITY_REPOSITORY_MAP = new ConcurrentHashMap<>();
    private static final List<RepositoryPostProcessor> REPOSITORY_POST_PROCESSORS = new ArrayList<>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (StringUtils.isNotBlank(beanClassName) && !beanClassName.startsWith("org.springframework.")) {
                Class<?> beanClass = ClassUtil.loadClass(beanClassName);
                if (AbstractContextRepository.class.isAssignableFrom(beanClass)) {
                    Class<?> entityClass = ReflectUtils.getFirstArgumentType(beanClass);
                    ENTITY_REPOSITORY_MAP.put(entityClass, beanClass);
                }
            }
        }
        Map<String, RepositoryPostProcessor> beansOfType = beanFactory.getBeansOfType(RepositoryPostProcessor.class);
        REPOSITORY_POST_PROCESSORS.addAll(beansOfType.values());
        AnnotationAwareOrderComparator.sort(REPOSITORY_POST_PROCESSORS);
    }

    public static Class<?> findRepositoryClass(Class<?> entityClass) {
        return ENTITY_REPOSITORY_MAP.get(entityClass);
    }

    public static List<RepositoryPostProcessor> getRepositoryPostProcessors() {
        return REPOSITORY_POST_PROCESSORS;
    }

}
