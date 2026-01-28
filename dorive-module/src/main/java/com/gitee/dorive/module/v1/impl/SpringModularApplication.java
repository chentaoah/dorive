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

package com.gitee.dorive.module.v1.impl;

import com.gitee.dorive.module.v1.api.ModuleParser;
import com.gitee.dorive.module.v1.entity.ModuleDefinition;
import com.gitee.dorive.module.v1.impl.bean.ModuleAnnotationBeanNameGenerator;
import com.gitee.dorive.module.v1.impl.factory.ModuleApplicationContextFactory;
import com.gitee.dorive.module.v1.impl.parser.DefaultModuleParser;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.*;

public class SpringModularApplication {

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return build(primarySource, args).run(args);
    }

    public static SpringApplicationBuilder build(Class<?> primarySource, String[] args) {
        ClassLoader classLoader = ModuleLauncher.INSTANCE.tryLoadClasspathIdx(primarySource);

        ApplicationArguments arguments = new DefaultApplicationArguments(args);
        Set<Class<?>> sources = new LinkedHashSet<>();
        Set<String> profiles = new LinkedHashSet<>();
        Map<String, Object> properties = new LinkedHashMap<>();

        sources.add(primarySource);
        properties.put("dorive.module.enable", true);

        ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
        moduleParser.parse(arguments);
        List<ModuleDefinition> moduleDefinitions = moduleParser.getModuleDefinitions();
        for (ModuleDefinition moduleDefinition : moduleDefinitions) {
            Class<?> mainClass = moduleDefinition.getMainClass();
            if (mainClass != null) {
                sources.add(mainClass);
            }
            profiles.addAll(moduleDefinition.getActiveProfiles());
            properties.put(moduleDefinition.getModulePathKey(), moduleDefinition.getModulePathValue());
        }

        SpringApplicationBuilder builder = new SpringApplicationBuilder(sources.toArray(new Class[0]))
                .profiles(profiles.toArray(new String[0]))
                .properties(properties)
                .beanNameGenerator(new ModuleAnnotationBeanNameGenerator())
                .contextFactory(new ModuleApplicationContextFactory());
        if (classLoader != null) {
            builder.resourceLoader(new DefaultResourceLoader(classLoader));
        }
        return builder;
    }

}
