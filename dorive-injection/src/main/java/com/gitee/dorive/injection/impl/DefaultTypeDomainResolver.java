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
package com.gitee.dorive.injection.impl;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.injection.annotation.Root;
import com.gitee.dorive.injection.api.TypeDomainResolver;
import com.gitee.dorive.injection.entity.DomainDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Objects;

public class DefaultTypeDomainResolver implements TypeDomainResolver {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");
    private final String scanPackage;
    private final List<DomainDefinition> domainDefinitions;

    public DefaultTypeDomainResolver(String scanPackage, List<DomainDefinition> domainDefinitions) {
        this.scanPackage = scanPackage;
        this.domainDefinitions = domainDefinitions;
    }

    @Override
    public boolean isUnderScanPackage(Class<?> typeToMatch) {
        return antPathMatcher.match(scanPackage, typeToMatch.getName());
    }

    @Override
    public DomainDefinition matchDomainDefinition(Class<?> typeToMatch) {
        return CollUtil.findOne(domainDefinitions, item -> antPathMatcher.match(item.getPattern(), typeToMatch.getName()));
    }

    @Override
    public void checkDomain(Class<?> targetType, Class<?> injectedType) {
        Root root = AnnotationUtils.getAnnotation(injectedType, Root.class);
        if (root != null) {
            return;
        }

        DomainDefinition injectedDomainDefinition = matchDomainDefinition(injectedType);
        if (injectedDomainDefinition == null) {
            return;
        }

        DomainDefinition targetDomainDefinition = matchDomainDefinition(targetType);
        if (targetDomainDefinition == null) {
            throwInjectionException(targetType, null, injectedType, injectedDomainDefinition.getName());
            return;
        }

        String targetDomainName = targetDomainDefinition.getName();
        String injectedDomainName = injectedDomainDefinition.getName();

        boolean isSameDomain = Objects.equals(targetDomainName, injectedDomainName);
        boolean isSubdomain = targetDomainName.startsWith(injectedDomainName + "-");

        if (!isSameDomain && !isSubdomain) {
            throwInjectionException(targetType, targetDomainName, injectedType, injectedDomainName);
        }
    }

    protected void throwInjectionException(Class<?> targetType, String targetDomainName, Class<?> injectedType, String injectedDomainName) {
        String message = String.format("Injection of autowired dependencies failed! targetType: [%s], targetDomain: [%s], injectedType: [%s], injectedDomain: [%s]",
                targetType.getName(), targetDomainName, injectedType.getName(), injectedDomainName);
        throw new BeanCreationException(message);
    }

    @Override
    public void checkDomainProtection(Class<?> targetType) {
        DomainDefinition domainDefinition = matchDomainDefinition(targetType);
        if (domainDefinition == null) {
            return;
        }

        String protect = domainDefinition.getProtect();
        String targetTypeName = targetType.getName();

        if (StringUtils.isNotBlank(protect) && antPathMatcher.match(protect, targetTypeName)) {
            String message = String.format("The type cannot be annotated by @Root! targetType: [%s], protect: [%s]", targetTypeName, protect);
            throw new BeanCreationException(message);
        }
    }

}
