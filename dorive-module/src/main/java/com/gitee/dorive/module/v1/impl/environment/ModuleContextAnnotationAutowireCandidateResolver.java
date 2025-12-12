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

package com.gitee.dorive.module.v1.impl.environment;

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
                if (moduleDefinition != null && !moduleDefinition.isGlobalValues(declaringClass)) {
                    String strValue = (String) value;
                    if (strValue.startsWith("${") && strValue.endsWith("}")) {
                        strValue = StrUtil.removePrefix(strValue, "${");
                        return "${" + moduleDefinition.getPropertiesPrefix() + strValue;
                    }
                }
            }
        }
        return value;
    }

}
