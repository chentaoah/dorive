/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.api;

import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;

public interface Endpoint {

    FieldDefinition getFieldDefinition();

    Object getValue(Object entity);

    void setValue(Object entity, Object value);

}
