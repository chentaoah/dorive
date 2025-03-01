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

package com.gitee.dorive.module.impl.parser;

import com.gitee.dorive.module.api.ModuleChecker;
import com.gitee.dorive.module.entity.ModuleDefinition;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;

public class DefaultModuleParser extends AbstractModuleParser implements ModuleChecker {

    public static final DefaultModuleParser INSTANCE = new DefaultModuleParser();

    @Override
    public void checkInjection(Class<?> type, Class<?> injectedType, Object injectedInstance) {
        doCheckInjection(type, injectedType);
        if (injectedInstance != null) {
            Class<?> targetClass = AopUtils.getTargetClass(injectedInstance);
            if (!injectedType.equals(targetClass)) {
                doCheckInjection(type, targetClass);
            }
        }
    }

    private void doCheckInjection(Class<?> type, Class<?> injectedType) {
        if (isUnderScanPackage(injectedType.getName())) {
            // 模块定义不存在，则判定为通过
            // 在公开的包路径下，则判定为通过
            ModuleDefinition moduleDefinition = findModuleDefinition(injectedType);
            if (moduleDefinition == null || moduleDefinition.isExposed(injectedType)) {
                return;
            }
            // 模块
            if (isUnderScanPackage(type.getName())) {
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

    private void throwInjectionException(Class<?> type, Class<?> injectedType) {
        String message = String.format("Injection of autowired dependencies failed! type: %s, injectedType: %s", type.getName(), injectedType.getName());
        throw new BeanCreationException(message);
    }

}
