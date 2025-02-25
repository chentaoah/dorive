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

import com.gitee.dorive.inject.api.ModuleChecker;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;

@AllArgsConstructor
public class LimitedCglibSubclassingInstantiationStrategy extends CglibSubclassingInstantiationStrategy {

    private final ModuleChecker moduleChecker;

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
        Class<?> resolvableType = (Class<?>) bd.getResolvableType().getType();
        if (isNotSpringInternalType(resolvableType) && moduleChecker.isUnderScanPackage(resolvableType)) {
            for (Class<?> parameterType : ctor.getParameterTypes()) {
                if (isNotSpringInternalType(parameterType) && moduleChecker.isUnderScanPackage(parameterType)) {
                    moduleChecker.checkInjection(resolvableType, parameterType);
                }
            }
        }
        return super.instantiate(bd, beanName, owner, ctor, args);
    }

    private boolean isNotSpringInternalType(Class<?> typeToMatch) {
        return !typeToMatch.getName().startsWith("org.springframework.");
    }

}
