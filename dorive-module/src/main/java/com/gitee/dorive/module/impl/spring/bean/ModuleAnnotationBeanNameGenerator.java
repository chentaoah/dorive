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

package com.gitee.dorive.module.impl.spring.bean;

import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
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
