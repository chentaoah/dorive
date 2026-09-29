/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.factory.api.entity;

import com.gitee.dorive.base.v1.factory.api.name.NameMapper;
import com.gitee.dorive.base.v1.factory.api.value.ValueMapper;

import java.util.List;
import java.util.Set;

public interface EntityMapper extends NameMapper, ValueMapper {

    List<String> serialize(List<String> names);

    Set<String> serialize(Set<String> names);

}
