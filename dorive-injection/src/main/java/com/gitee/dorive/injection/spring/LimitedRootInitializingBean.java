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

import com.gitee.dorive.injection.annotation.Root;
import com.gitee.dorive.injection.api.TypeDomainResolver;
import com.gitee.dorive.injection.utils.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class LimitedRootInitializingBean implements ApplicationContextAware, InitializingBean {

    private final TypeDomainResolver typeDomainResolver;
    private ApplicationContext applicationContext;

    public LimitedRootInitializingBean(TypeDomainResolver typeDomainResolver) {
        this.typeDomainResolver = typeDomainResolver;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Root.class);
        beansWithAnnotation.forEach((name, bean) -> {
            Class<?> targetClass = AopUtils.getAnnotatedClass(bean, Root.class);
            typeDomainResolver.checkProtection(targetClass);
        });
    }

}
