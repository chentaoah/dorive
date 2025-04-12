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

package com.gitee.dorive.module.impl.factory;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.api.entity.common.BoundedContext;
import com.gitee.dorive.module.api.BeanNameEditor;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.environment.ModuleContextAnnotationAutowireCandidateResolver;
import com.gitee.dorive.module.impl.inject.ModuleCglibSubclassingInstantiationStrategy;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.impl.util.ConfigurationUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

import java.lang.reflect.Method;
import java.util.*;

import static com.gitee.dorive.module.impl.util.BeanAnnotationHelper.BEAN_NAME_CACHE;

@Getter
@Setter
public class ModuleDefaultListableBeanFactory extends DefaultListableBeanFactory implements BeanNameEditor {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;

    public ModuleDefaultListableBeanFactory() {
        // 实例化策略
        setInstantiationStrategy(new ModuleCglibSubclassingInstantiationStrategy());
        // 依赖注入解析器
        setAutowireCandidateResolver(new ModuleContextAnnotationAutowireCandidateResolver());
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        if (ConfigurationUtils.isConfigurationBeanDefinition(beanDefinition)) {
            beanName = resetBeanName(beanName, beanDefinition, this);
        }
        super.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public String resetBeanName(String beanName, BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        String factoryBeanName = beanDefinition.getFactoryBeanName();
        AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
        MethodMetadata factoryMethodMetadata = (MethodMetadata) ReflectUtil.getFieldValue(beanDefinition, "factoryMethodMetadata");
        String derivedBeanName = (String) ReflectUtil.getFieldValue(beanDefinition, "derivedBeanName");
        if (StringUtils.isNotBlank(factoryBeanName) && annotationMetadata != null && factoryMethodMetadata != null && StringUtils.isNotBlank(derivedBeanName)) {
            // 类型
            String className = annotationMetadata.getClassName();
            if (moduleParser.isUnderScanPackage(className)) {
                Class<?> configurationClass = ClassUtil.loadClass(className);
                // 方法
                String methodName = factoryMethodMetadata.getMethodName();
                Method factoryMethod = ReflectUtil.getMethod(configurationClass, methodName);
                // @Bean注解
                Map<String, Object> annotationAttributes = factoryMethodMetadata.getAnnotationAttributes(Bean.class.getName());
                if (annotationAttributes != null) {
                    Object name = annotationAttributes.get("name");
                    if (name instanceof String[] && ((String[]) name).length == 0) {
                        String newDerivedBeanName = factoryBeanName + "." + derivedBeanName;
                        BEAN_NAME_CACHE.put(factoryMethod, newDerivedBeanName);
                        ReflectUtil.setFieldValue(beanDefinition, "derivedBeanName", newDerivedBeanName);
                        return newDerivedBeanName;
                    }
                }
            }
        }
        return beanName;
    }

    @Override
    protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
        String beanName = super.determineAutowireCandidate(candidates, descriptor);
        if (beanName == null && candidates.size() > 1) {
            Class<?> declaringClass = (Class<?>) ReflectUtil.getFieldValue(descriptor, "declaringClass");
            if (moduleParser.isUnderScanPackage(declaringClass.getName())) {
                ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(declaringClass);
                if (moduleDefinition != null) {
                    List<String> candidateBeanNames = new ArrayList<>(candidates.size());
                    Set<String> exposedCandidateBeanNames = new HashSet<>(candidates.size() * 4 / 3 + 1);
                    for (String candidateBeanName : candidates.keySet()) {
                        Class<?> targetClass = null;
                        // class of factory bean
                        BeanDefinition beanDefinition = getBeanDefinition(candidateBeanName);
                        if (ConfigurationUtils.isConfigurationBeanDefinition(beanDefinition)) {
                            AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
                            String className = annotationMetadata.getClassName();
                            targetClass = ClassUtil.loadClass(className);
                        }
                        // class of bean
                        if (targetClass == null) {
                            targetClass = (Class<?>) candidates.get(candidateBeanName);
                        }
                        if (moduleParser.isUnderScanPackage(targetClass.getName())) {
                            ModuleDefinition targetModuleDefinition = moduleParser.findModuleDefinition(targetClass);
                            if (moduleDefinition.equals(targetModuleDefinition)) { // 1、相同模块
                                candidateBeanNames.add(candidateBeanName);

                            } else if (targetModuleDefinition.isExposed(targetClass)) { // 2、其他模块对外公开
                                exposedCandidateBeanNames.add(candidateBeanName);
                            }
                        }
                    }
                    // 1、相同模块
                    if (candidateBeanNames.size() == 1) {
                        return candidateBeanNames.get(0);
                    }
                    // 2、其他模块对外公开
                    if (candidateBeanNames.isEmpty()) {
                        Class<?> declaredType = descriptor.getDeclaredType();
                        if (declaredType == BoundedContext.class) {
                            if (!exposedCandidateBeanNames.isEmpty()) {
                                String domainPackage = moduleDefinition.getDomainPackage();
                                String boundedContextName = domainPackage + ".boundedContext";
                                if (exposedCandidateBeanNames.contains(boundedContextName)) {
                                    return boundedContextName;
                                }
                            }
                        } else {
                            if (exposedCandidateBeanNames.size() == 1) {
                                return exposedCandidateBeanNames.iterator().next();
                            }
                        }
                    }
                }
            }
        }
        return beanName;
    }

}
