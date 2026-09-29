/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
public class ModuleBeanDescriptor {
    private ModuleDefinition moduleDefinition;
    @Nullable
    private String beanName;
    @Nullable
    private Class<?> factoryBeanType;
    private Class<?> beanType;
}
