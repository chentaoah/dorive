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

package com.gitee.dorive.module.impl.runner;

import com.gitee.dorive.api.api.common.BoundedContext;
import com.gitee.dorive.module.api.ModuleParser;
import com.gitee.dorive.module.entity.ModuleDefinition;
import com.gitee.dorive.module.impl.parser.DefaultModuleParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URL;
import java.util.List;

@Getter
@Setter
public class ModuleApplicationRunner implements ApplicationContextAware, ApplicationRunner {

    private ModuleParser moduleParser = DefaultModuleParser.INSTANCE;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<ModuleDefinition> moduleDefinitions = moduleParser.getModuleDefinitions();
        for (ModuleDefinition moduleDefinition : moduleDefinitions) {
            String domainPath = moduleDefinition.getDomainPath();
            String boundedContextName = domainPath + ".boundedContext";
            if (applicationContext.containsBean(boundedContextName)) {
                Object bean = applicationContext.getBean(boundedContextName);
                if (bean instanceof BoundedContext) {
                    BoundedContext boundedContext = (BoundedContext) bean;
                    List<String> requires = moduleDefinition.getRequires();
                    List<String> provides = moduleDefinition.getProvides();
                    checkResources(boundedContext, requires);
                    checkResources(boundedContext, provides);
                }
            }
        }
    }

    private void checkResources(BoundedContext boundedContext, List<String> names) {
        if (names != null) {
            for (String name : names) {
                URL resource = boundedContext.getResource(name);
                if (resource == null) {
                    throw new RuntimeException("The resource does not exist! name: " + name);
                }
            }
        }
    }

}
