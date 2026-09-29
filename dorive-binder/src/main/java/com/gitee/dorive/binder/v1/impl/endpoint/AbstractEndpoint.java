/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.endpoint;

import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;
import com.gitee.dorive.binder.v1.api.Endpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractEndpoint implements Endpoint {
    private FieldDefinition fieldDefinition;
}
