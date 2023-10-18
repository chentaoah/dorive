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

package com.gitee.dorive.env.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;

public class KeyValuesConfiguration extends KeyValuesEnvPostProcessor implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initialize() {
        if (instance != null) {
            BeanUtil.copyProperties(instance, this);
        } else {
            ReflectionUtils.doWithLocalFields(this.getClass(), declaredField -> {
                if (Modifier.isStatic(declaredField.getModifiers())) {
                    return;
                }
                Object value = determineValue(environment, declaredField);
                if (value != null) {
                    ReflectUtil.setFieldValue(this, declaredField, value);
                }
            });
        }
    }

}
