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

package com.gitee.dorive.module.impl;

import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.factory.ModuleApplicationContextFactory;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import com.gitee.dorive.module.impl.bean.ModuleAnnotationBeanNameGenerator;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;

public class SpringModularApplication {

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return build(primarySource).run(args);
    }

    public static SpringApplicationBuilder build(Class<?> primarySource) {
        ModuleLauncher.INSTANCE.tryLoadClasspathIdx(primarySource);

        Set<Class<?>> sources = new LinkedHashSet<>();
        Set<String> profiles = new LinkedHashSet<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        BeanNameGenerator beanNameGenerator = new ModuleAnnotationBeanNameGenerator();
        ApplicationContextFactory applicationContextFactory = new ModuleApplicationContextFactory();

        ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
        moduleParser.parse();
        List<ModuleDefinition> moduleDefinitions = moduleParser.getModuleDefinitions();

        sources.add(primarySource);
        properties.put("dorive.module.enable", true);
        for (ModuleDefinition moduleDefinition : moduleDefinitions) {
            Class<?> mainClass = moduleDefinition.getMainClass();
            if (mainClass != null) {
                sources.add(mainClass);
            }
            profiles.addAll(moduleDefinition.getActiveProfiles());
            properties.put(moduleDefinition.getModulePathKey(), moduleDefinition.getModulePathValue());
        }

        return new SpringApplicationBuilder(sources.toArray(new Class[0]))
                .profiles(profiles.toArray(new String[0]))
                .properties(properties)
                .beanNameGenerator(beanNameGenerator)
                .contextFactory(applicationContextFactory);
    }

}
