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

package com.gitee.dorive.inject.impl;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.inject.api.ModuleInjectionLimiter;
import com.gitee.dorive.inject.entity.ExportDefinition;
import com.gitee.dorive.inject.entity.ModuleDefinition;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.AntPathMatcher;

import java.util.List;

public class DefaultModuleInjectionLimiter implements ModuleInjectionLimiter {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");
    private final String scanPackage;
    private final List<ModuleDefinition> moduleDefinitions;

    public DefaultModuleInjectionLimiter(String scanPackage, List<ModuleDefinition> moduleDefinitions) {
        this.scanPackage = scanPackage;
        this.moduleDefinitions = moduleDefinitions;
    }

    @Override
    public boolean isUnderScanPackage(Class<?> typeToMatch) {
        return antPathMatcher.match(scanPackage, typeToMatch.getName());
    }

    @Override
    public void checkInjectedType(Class<?> type, Class<?> injectedType) {
        if (isUnderScanPackage(injectedType)) {
            ModuleDefinition moduleDefinition = findModuleDefinition(injectedType);
            // 模块定义不存在，则判定为通过
            if (moduleDefinition == null) {
                return;
            }
            // 在公开的包路径下，则判定为通过
            String typeName = injectedType.getName();
            List<ExportDefinition> exportDefinitions = moduleDefinition.getExports();
            for (ExportDefinition exportDefinition : exportDefinitions) {
                String path = exportDefinition.getPath();
                if (antPathMatcher.match(path, typeName)) {
                    return;
                }
            }
            // 模块
            if (isUnderScanPackage(type)) {
                ModuleDefinition thisModuleDefinition = findModuleDefinition(type);
                if (thisModuleDefinition != null) {
                    String thisModuleName = thisModuleDefinition.getName();
                    String moduleName = moduleDefinition.getName();
                    // 相同模块
                    if (thisModuleName.equals(moduleName)) {
                        return;
                    }
                    // 子模块允许引用父模块
                    if (thisModuleName.startsWith(moduleName + "-") || thisModuleName.startsWith(moduleName + "_")) {
                        return;
                    }
                }
            }
            // 抛出异常
            throwInjectionException(type, injectedType);
        }
    }

    private ModuleDefinition findModuleDefinition(Class<?> typeToMatch) {
        return CollUtil.findOne(moduleDefinitions, item -> antPathMatcher.match(item.getPath(), typeToMatch.getName()));
    }

    private void throwInjectionException(Class<?> type, Class<?> injectedType) {
        String message = String.format("Injection of autowired dependencies failed! type: %s, injectedType: %s", type.getName(), injectedType.getName());
        throw new BeanCreationException(message);
    }

}
