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

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.env.annotation.Construct;
import com.gitee.dorive.env.util.AopUtils;
import lombok.Data;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class ConstructResolver {

    private Environment environment;
    private Set<String> activeProfiles;

    public ConstructResolver(Environment environment) {
        this.environment = environment;
        this.activeProfiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));
    }

    public void initialize(Object instance) {
        Class<?> targetClass = AopUtils.getTargetClass(instance);
        ReflectionUtils.doWithLocalMethods(targetClass, declaredMethod -> {
            if (Modifier.isStatic(declaredMethod.getModifiers())) {
                return;
            }
            Construct constructAnnotation = declaredMethod.getAnnotation(Construct.class);
            if (constructAnnotation != null) {
                if (matchEnvironment(constructAnnotation, declaredMethod)) {
                    ReflectUtil.invoke(instance, declaredMethod);
                }
            }
        });
    }

    protected boolean matchEnvironment(Construct constructAnnotation, Method declaredMethod) {
        String[] profile = constructAnnotation.profile();
        Set<String> profiles = new LinkedHashSet<>(Arrays.asList(profile));
        return profiles.isEmpty() || !Collections.disjoint(activeProfiles, profiles);
    }

}
