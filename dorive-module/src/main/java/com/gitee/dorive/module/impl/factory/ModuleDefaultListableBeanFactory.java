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
import com.gitee.dorive.module.api.BeanNameEditor;
import com.gitee.dorive.module.api.ExposedBeanFilter;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleBeanDescriptor;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.environment.ModuleContextAnnotationAutowireCandidateResolver;
import com.gitee.dorive.module.impl.inject.ModuleCglibSubclassingInstantiationStrategy;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.util.SpringClassUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gitee.dorive.module.util.BeanAnnotationHelper.BEAN_NAME_CACHE;

@Getter
@Setter
public class ModuleDefaultListableBeanFactory extends DefaultListableBeanFactory implements BeanNameEditor {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
    private List<ExposedBeanFilter> exposedBeanFilters;

    public ModuleDefaultListableBeanFactory() {
        // 实例化策略
        setInstantiationStrategy(new ModuleCglibSubclassingInstantiationStrategy());
        // 依赖注入解析器
        setAutowireCandidateResolver(new ModuleContextAnnotationAutowireCandidateResolver());
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        if (SpringClassUtils.isConfigurationBeanDefinition(beanDefinition)) {
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
    protected Map<String, Object> findAutowireCandidates(String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {
        Map<String, Object> candidates = super.findAutowireCandidates(beanName, requiredType, descriptor);
        if (candidates.size() == 1) {
            if (!SpringClassUtils.isStreamDependencyDescriptor(descriptor) && !SpringClassUtils.isMultiElementDescriptor(descriptor)) {
                Class<?> declaringClass = getDeclaringClass(descriptor);
                if (moduleParser.isUnderScanPackage(declaringClass.getName())) {
                    ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(declaringClass);
                    if (moduleDefinition != null) {
                        String candidateBeanName = candidates.keySet().iterator().next();
                        Class<?> targetClass = getTargetClass(candidates, candidateBeanName);
                        Class<?> targetDeclaringClass = getTargetDeclaringClass(candidateBeanName, targetClass);

                        if (moduleParser.isUnderScanPackage(targetDeclaringClass.getName())) {
                            ModuleDefinition targetModuleDefinition = moduleParser.findModuleDefinition(targetDeclaringClass);
                            if (moduleDefinition.equals(targetModuleDefinition)) { // 1、相同模块
                                return candidates;

                            } else if (targetModuleDefinition.isExposed(targetDeclaringClass)) { // 2、其他模块对外公开
                                ModuleBeanDescriptor beanDescriptor = new ModuleBeanDescriptor(moduleDefinition, beanName, null, declaringClass);
                                Map<String, ModuleBeanDescriptor> exposedCandidates = new HashMap<>(2);
                                exposedCandidates.put(candidateBeanName, new ModuleBeanDescriptor(targetModuleDefinition, candidateBeanName, targetDeclaringClass, targetClass));
                                // 过滤
                                invokeExposedBeanFilters(descriptor, beanDescriptor, exposedCandidates);
                                if (exposedCandidates.isEmpty()) {
                                    candidates.clear();
                                }
                            }
                        }
                    }
                }
            }
        }
        return candidates;
    }

    @Override
    protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
        String beanName = super.determineAutowireCandidate(candidates, descriptor);
        if (beanName == null && candidates.size() > 1) {
            Class<?> declaringClass = getDeclaringClass(descriptor);
            if (moduleParser.isUnderScanPackage(declaringClass.getName())) {
                ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(declaringClass);
                if (moduleDefinition != null) {
                    List<String> candidateBeanNames = new ArrayList<>(candidates.size());
                    ModuleBeanDescriptor beanDescriptor = new ModuleBeanDescriptor(moduleDefinition, null, null, declaringClass);
                    Map<String, ModuleBeanDescriptor> exposedCandidates = new HashMap<>(candidates.size() * 4 / 3 + 1);

                    for (String candidateBeanName : candidates.keySet()) {
                        Class<?> targetClass = getTargetClass(candidates, candidateBeanName);
                        Class<?> targetDeclaringClass = getTargetDeclaringClass(candidateBeanName, targetClass);

                        if (moduleParser.isUnderScanPackage(targetDeclaringClass.getName())) {
                            ModuleDefinition targetModuleDefinition = moduleParser.findModuleDefinition(targetDeclaringClass);
                            if (moduleDefinition.equals(targetModuleDefinition)) { // 1、相同模块
                                candidateBeanNames.add(candidateBeanName);

                            } else if (targetModuleDefinition.isExposed(targetDeclaringClass)) { // 2、其他模块对外公开
                                exposedCandidates.put(candidateBeanName, new ModuleBeanDescriptor(targetModuleDefinition, candidateBeanName, targetDeclaringClass, targetClass));
                            }
                        }
                    }

                    // 1、相同模块
                    if (candidateBeanNames.size() == 1) {
                        return candidateBeanNames.get(0);
                    }
                    // 2、其他模块对外公开
                    if (candidateBeanNames.isEmpty()) {
                        // 过滤
                        invokeExposedBeanFilters(descriptor, beanDescriptor, exposedCandidates);
                        if (exposedCandidates.size() == 1) {
                            return exposedCandidates.keySet().iterator().next();
                        }
                    }
                }
            }
        }
        return beanName;
    }

    private Class<?> getDeclaringClass(DependencyDescriptor descriptor) {
        Class<?> declaringClass = (Class<?>) ReflectUtil.getFieldValue(descriptor, "containingClass");
        if (declaringClass == null) {
            declaringClass = (Class<?>) ReflectUtil.getFieldValue(descriptor, "declaringClass");
        }
        return declaringClass;
    }

    private Class<?> getTargetClass(Map<String, Object> candidates, String candidateBeanName) {
        Object candidate = candidates.get(candidateBeanName);
        return candidate instanceof Class ? (Class<?>) candidate : ClassUtils.getUserClass(candidate);
    }

    private Class<?> getTargetDeclaringClass(String candidateBeanName, Class<?> targetClass) {
        BeanDefinition beanDefinition = getBeanDefinition(candidateBeanName);
        if (SpringClassUtils.isConfigurationBeanDefinition(beanDefinition)) {
            AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
            String className = annotationMetadata.getClassName();
            return ClassUtil.loadClass(className);
        }
        return targetClass;
    }

    private void invokeExposedBeanFilters(DependencyDescriptor descriptor, ModuleBeanDescriptor beanDescriptor, Map<String, ModuleBeanDescriptor> exposedCandidates) {
        if (exposedBeanFilters == null) {
            synchronized (this) {
                if (exposedBeanFilters == null) {
                    String[] beanNames = getBeanNamesForType(ExposedBeanFilter.class, true, false);
                    this.exposedBeanFilters = new ArrayList<>(beanNames.length);
                    for (String beanName : beanNames) {
                        ExposedBeanFilter exposedBeanFilter = getBean(beanName, ExposedBeanFilter.class);
                        exposedBeanFilters.add(exposedBeanFilter);
                    }
                    AnnotationAwareOrderComparator.sort(exposedBeanFilters);
                }
            }
        }
        for (ExposedBeanFilter exposedBeanFilter : exposedBeanFilters) {
            exposedBeanFilter.filterExposedCandidates(descriptor, beanDescriptor, exposedCandidates);
        }
    }

}
