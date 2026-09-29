/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.api;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface BeanNameEditor {

    String resetBeanName(String beanName, BeanDefinition beanDefinition, BeanDefinitionRegistry registry);

}
