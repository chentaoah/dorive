/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
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
