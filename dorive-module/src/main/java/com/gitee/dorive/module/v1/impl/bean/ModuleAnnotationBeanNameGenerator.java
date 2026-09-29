/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.bean;

import com.gitee.dorive.module.v1.api.ModuleParser;
import com.gitee.dorive.module.v1.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

@Getter
@Setter
public class ModuleAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;

    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        if (beanClassName != null && moduleParser.isUnderScanPackage(beanClassName)) {
            return beanClassName;
        }
        return super.buildDefaultBeanName(definition);
    }

}
