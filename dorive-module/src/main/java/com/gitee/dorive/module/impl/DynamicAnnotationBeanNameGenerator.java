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

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashSet;
import java.util.Set;

public class DynamicAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");
    private final Set<String> scanPackages;

    public DynamicAnnotationBeanNameGenerator(String scanPackages) {
        this.scanPackages = new LinkedHashSet<>(StrUtil.splitTrim(scanPackages, ","));
    }

    private boolean isUnderScanPackage(String className) {
        for (String scanPackage : scanPackages) {
            if (antPathMatcher.match(scanPackage, className)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        if (beanClassName != null && isUnderScanPackage(beanClassName)) {
            return beanClassName;
        }
        return super.buildDefaultBeanName(definition);
    }

}
