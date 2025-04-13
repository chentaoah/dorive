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

package com.gitee.dorive.module.impl.filter;

import com.gitee.dorive.api.entity.BoundedContext;
import com.gitee.dorive.module.api.ExposedBeanFilter;
import com.gitee.dorive.module.entity.ModuleBeanDescriptor;
import com.gitee.dorive.module.entity.ModuleDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;

import java.util.Map;

public class BoundedContextExposedBeanFilter implements ExposedBeanFilter {

    @Override
    public void filterExposedCandidates(DependencyDescriptor descriptor, ModuleBeanDescriptor beanDescriptor, Map<String, ModuleBeanDescriptor> exposedCandidates) {
        Class<?> declaredType = descriptor.getDeclaredType();
        if (declaredType == BoundedContext.class && !exposedCandidates.isEmpty()) {
            ModuleDefinition moduleDefinition = beanDescriptor.getModuleDefinition();
            String domainPackage = moduleDefinition.getDomainPackage();
            String boundedContextName = domainPackage + ".boundedContext";
            ModuleBeanDescriptor existModuleBeanDescriptor = exposedCandidates.get(boundedContextName);
            // 清空
            exposedCandidates.clear();
            // 保留符合条件的
            if (existModuleBeanDescriptor != null) {
                exposedCandidates.put(boundedContextName, existModuleBeanDescriptor);
            }
        }
    }

}
