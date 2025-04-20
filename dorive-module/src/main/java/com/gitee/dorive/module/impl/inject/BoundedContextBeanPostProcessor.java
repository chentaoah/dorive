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

package com.gitee.dorive.module.impl.inject;

import com.gitee.dorive.api.api.BoundedContextAware;
import com.gitee.dorive.api.entity.BoundedContext;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.ClassUtils;

@Getter
@Setter
public class BoundedContextBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> userClass = ClassUtils.getUserClass(bean);
        if (moduleParser.isUnderScanPackage(userClass.getName())) {
            if (bean instanceof BoundedContextAware) {
                ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(userClass);
                if (moduleDefinition != null) {
                    String domainPackage = moduleDefinition.getDomainPackage();
                    String boundedContextName = domainPackage + ".boundedContext";
                    if (beanFactory.containsBean(boundedContextName)) {
                        BoundedContext boundedContext = beanFactory.getBean(boundedContextName, BoundedContext.class);
                        ((BoundedContextAware) bean).setBoundedContext(boundedContext);
                    }
                }
            }
        }
        return bean;
    }

}
