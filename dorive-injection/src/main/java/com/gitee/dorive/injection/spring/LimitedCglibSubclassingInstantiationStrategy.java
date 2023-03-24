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

package com.gitee.dorive.injection.spring;

import com.gitee.dorive.injection.api.TypeDomainResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;

public class LimitedCglibSubclassingInstantiationStrategy extends CglibSubclassingInstantiationStrategy {

    private TypeDomainResolver typeDomainResolver;

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
        tryGetResolverFromContext(owner);
        if (typeDomainResolver != null) {
            Class<?> resolvableType = (Class<?>) bd.getResolvableType().getType();
            if (isNotSpringInternalType(resolvableType) && typeDomainResolver.isUnderScanPackage(resolvableType)) {
                for (Class<?> parameterType : ctor.getParameterTypes()) {
                    if (isNotSpringInternalType(parameterType) && typeDomainResolver.isUnderScanPackage(parameterType)) {
                        typeDomainResolver.checkDomain(resolvableType, parameterType);
                    }
                }
            }
        }
        return super.instantiate(bd, beanName, owner, ctor, args);
    }

    private void tryGetResolverFromContext(BeanFactory owner) {
        if (typeDomainResolver == null) {
            synchronized (this) {
                if (typeDomainResolver == null) {
                    typeDomainResolver = owner.getBean(TypeDomainResolver.class);
                }
            }
        }
    }

    private boolean isNotSpringInternalType(Class<?> typeToMatch) {
        return !typeToMatch.getName().startsWith("org.springframework.");
    }

}
