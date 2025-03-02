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
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.impl.spring.bean.ModuleConfigurationBeanNameEditor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

import static com.gitee.dorive.module.impl.spring.bean.ModuleConfigurationBeanNameEditor.CONFIGURATION_CLASS_BEAN_DEFINITION_CLASS_NAME;

@Getter
@Setter
public class ModuleDefaultListableBeanFactory extends DefaultListableBeanFactory {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
    private BeanNameEditor beanNameEditor = new ModuleConfigurationBeanNameEditor();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        String finalBeanName = beanNameEditor.resetBeanName(beanName, beanDefinition, this);
        super.registerBeanDefinition(finalBeanName, beanDefinition);
    }

    @Override
    protected String determineAutowireCandidate(Map<String, Object> candidates, DependencyDescriptor descriptor) {
        String beanName = super.determineAutowireCandidate(candidates, descriptor);
        if (beanName == null && candidates.size() > 1) {
            Class<?> declaringClass = (Class<?>) ReflectUtil.getFieldValue(descriptor, "declaringClass");
            if (moduleParser.isUnderScanPackage(declaringClass.getName())) {
                ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(declaringClass);
                for (String candidateBeanName : candidates.keySet()) {
                    Class<?> targetClass = null;
                    // class of factory bean
                    BeanDefinition beanDefinition = getBeanDefinition(candidateBeanName);
                    if (CONFIGURATION_CLASS_BEAN_DEFINITION_CLASS_NAME.equals(beanDefinition.getClass().getName())) {
                        AnnotationMetadata annotationMetadata = (AnnotationMetadata) ReflectUtil.getFieldValue(beanDefinition, "annotationMetadata");
                        String className = annotationMetadata.getClassName();
                        if (moduleParser.isUnderScanPackage(className)) {
                            targetClass = ClassUtil.loadClass(className);
                        }
                    }
                    // class of bean
                    if (targetClass == null) {
                        targetClass = (Class<?>) candidates.get(candidateBeanName);
                    }
                    ModuleDefinition targetModuleDefinition = moduleParser.findModuleDefinition(targetClass);
                    // 当存在多个候选时，相同模块的Bean将被选出
                    if (moduleDefinition.equals(targetModuleDefinition)) {
                        return candidateBeanName;
                    }
                }
            }
        }
        return beanName;
    }

}
