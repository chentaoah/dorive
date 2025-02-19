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
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.inject.api.ModuleChecker;
import com.gitee.dorive.inject.entity.ExportDefinition;
import com.gitee.dorive.inject.entity.ModuleDefinition;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultModuleChecker implements ModuleChecker {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(".");
    private final Set<String> scanPackages;
    private final List<ModuleDefinition> moduleDefinitions;

    public DefaultModuleChecker(String scanPackages, List<ModuleDefinition> moduleDefinitions) {
        this.scanPackages = new LinkedHashSet<>(StrUtil.splitTrim(scanPackages, ","));
        this.moduleDefinitions = moduleDefinitions;
    }

    @Override
    public boolean isUnderScanPackage(Class<?> typeToMatch) {
        for (String scanPackage : scanPackages) {
            if (antPathMatcher.match(scanPackage, typeToMatch.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkInjection(Class<?> type, Class<?> injectedType) {
        if (isUnderScanPackage(injectedType)) {
            // 模块定义不存在，则判定为通过
            ModuleDefinition moduleDefinition = findModuleDefinition(injectedType);
            if (moduleDefinition == null) {
                return;
            }
            // 在公开的包路径下，则判定为通过
            ExportDefinition exportDefinition = findExportDefinition(injectedType, moduleDefinition);
            if (exportDefinition != null) {
                return;
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

    private ExportDefinition findExportDefinition(Class<?> typeToMatch, ModuleDefinition moduleDefinition) {
        return CollUtil.findOne(moduleDefinition.getExports(), item -> antPathMatcher.match(item.getPath(), typeToMatch.getName()));
    }

    private void throwInjectionException(Class<?> type, Class<?> injectedType) {
        String message = String.format("Injection of autowired dependencies failed! type: %s, injectedType: %s", type.getName(), injectedType.getName());
        throw new BeanCreationException(message);
    }

}
