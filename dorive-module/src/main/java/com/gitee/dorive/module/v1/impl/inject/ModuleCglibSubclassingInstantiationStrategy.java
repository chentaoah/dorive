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

package com.gitee.dorive.module.v1.impl.inject;

import com.gitee.dorive.module.v1.api.ModuleChecker;
import com.gitee.dorive.module.v1.api.ModuleParser;
import com.gitee.dorive.module.v1.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.v1.impl.util.BeanFactoryUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;

@Getter
@Setter
public class ModuleCglibSubclassingInstantiationStrategy extends CglibSubclassingInstantiationStrategy {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
    private ModuleChecker moduleChecker = DefaultModuleParser.INSTANCE;

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) {
        Class<?> resolvableType = (Class<?>) bd.getResolvableType().getType();
        if (moduleParser.isUnderScanPackage(resolvableType.getName())) {
            Class<?>[] parameterTypes = ctor.getParameterTypes();
            for (int index = 0; index < parameterTypes.length; index++) {
                Class<?> parameterType = parameterTypes[index];
                Object arg = args[index];
                if (owner instanceof DefaultListableBeanFactory) {
                    Class<?> configurationClass = BeanFactoryUtils.tryGetConfigurationClass(
                            (DefaultListableBeanFactory) owner, parameterType, arg);
                    if (configurationClass != null) {
                        parameterType = configurationClass;
                        arg = null;
                    }
                }
                moduleChecker.checkInjection(resolvableType, parameterType, arg);
            }
        }
        return super.instantiate(bd, beanName, owner, ctor, args);
    }

}
