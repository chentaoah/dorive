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

package com.gitee.dorive.test.impl;

import com.gitee.dorive.module.impl.SpringModularApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.MergedContextConfiguration;

public class SpringBootModularContextLoader extends SpringBootContextLoader {

    private Class<?> primarySource;

    @Override
    public ApplicationContext loadContext(MergedContextConfiguration config) throws Exception {
        Class<?>[] configClasses = config.getClasses();
        if (configClasses.length > 0) {
            for (Class<?> configClass : configClasses) {
                SpringBootApplication annotation = AnnotationUtils.getAnnotation(configClass, SpringBootApplication.class);
                if (annotation != null) {
                    primarySource = configClass;
                    break;
                }
            }
        }
        return super.loadContext(config);
    }

    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplicationBuilder builder = SpringModularApplication.build(primarySource);
        return builder.build();
    }

}
