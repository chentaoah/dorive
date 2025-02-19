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

package com.gitee.dorive.inject.spring;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.inject.api.ModuleChecker;
import lombok.AllArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;

@AllArgsConstructor
public class LimitedAutowiredBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor, PriorityOrdered {

    private static final int order = Ordered.LOWEST_PRECEDENCE;
    private final ModuleChecker moduleChecker;

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        Class<?> beanType = AopUtils.getTargetClass(bean);
        if (moduleChecker.isNotSpringInternalType(beanType) && moduleChecker.isUnderScanPackage(beanType)) {
            try {
                checkAutowiredFieldModule(beanType, bean);

            } catch (BeanCreationException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
            }
        }
        return pvs;
    }

    private void checkAutowiredFieldModule(Class<?> beanType, Object bean) {
        ReflectionUtils.doWithLocalFields(beanType, field -> {
            AnnotationAttributes ann = findAutowiredAnnotation(field);
            if (ann != null && !Modifier.isStatic(field.getModifiers())) {
                Object fieldValue = ReflectUtil.getFieldValue(bean, field);
                moduleChecker.checkInjection(beanType, field.getType(), fieldValue);
            }
        });
    }

    private AnnotationAttributes findAutowiredAnnotation(AccessibleObject ao) {
        return ao.getAnnotations().length > 0 ? AnnotatedElementUtils.getMergedAnnotationAttributes(ao, Autowired.class) : null;
    }

}
