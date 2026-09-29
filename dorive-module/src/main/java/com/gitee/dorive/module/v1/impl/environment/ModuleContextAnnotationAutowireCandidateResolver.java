/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.environment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.module.v1.api.ModuleParser;
import com.gitee.dorive.module.v1.entity.ModuleDefinition;
import com.gitee.dorive.module.v1.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;

@Getter
@Setter
public class ModuleContextAnnotationAutowireCandidateResolver extends ContextAnnotationAutowireCandidateResolver {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;

    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        Object value = super.getSuggestedValue(descriptor);
        if (value instanceof String) {
            Class<?> declaringClass = (Class<?>) ReflectUtil.getFieldValue(descriptor, "declaringClass");
            if (declaringClass != null && moduleParser.isUnderScanPackage(declaringClass.getName())) {
                ModuleDefinition moduleDefinition = moduleParser.findModuleDefinition(declaringClass);
                if (moduleDefinition != null) {
                    if (CollUtil.isNotEmpty(moduleDefinition.getConfigs()) && !moduleDefinition.isGlobalValues(declaringClass)) {
                        String strValue = (String) value;
                        if (strValue.startsWith("${") && strValue.endsWith("}")) {
                            strValue = StrUtil.removePrefix(strValue, "${");
                            return "${" + moduleDefinition.getPropertiesPrefix() + strValue;
                        }
                    }
                }
            }
        }
        return value;
    }

}
