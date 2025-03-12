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

import cn.hutool.core.util.ClassLoaderUtil;
import com.gitee.dorive.module.impl.SpringModularApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfigurationAttributes;

public class SpringBootModularContextLoader extends SpringBootContextLoader {

    private static Class<?> primarySource;

    @Override
    public void processContextConfiguration(ContextConfigurationAttributes configAttributes) {
        super.processContextConfiguration(configAttributes);
        if (primarySource == null) {
            synchronized (SpringBootModularContextLoader.class) {
                if (primarySource == null) {
                    Class<?> declaringClass = configAttributes.getDeclaringClass();
                    String packageName = declaringClass.getPackage().getName();
                    primarySource = ClassLoaderUtil.loadClass(packageName + ".Application");
                }
            }
        }
    }

    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplicationBuilder builder = SpringModularApplication.build(primarySource);
        return builder.build();
    }

}
