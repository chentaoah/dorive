/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.api;

import com.gitee.dorive.module.v1.entity.ModuleBeanDescriptor;
import org.springframework.beans.factory.config.DependencyDescriptor;

import java.util.Map;

public interface ExposedBeanFilter {

    void filterExposedCandidates(DependencyDescriptor descriptor, ModuleBeanDescriptor beanDescriptor, Map<String, ModuleBeanDescriptor> exposedCandidates);

}
