/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.endpoint;

import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindEndpoint extends SpELEndpoint {
    private String belongAccessPath;
    private RepositoryItem belongRepository;

    public BindEndpoint(FieldDefinition fieldDefinition, String expression) {
        super(fieldDefinition, expression);
    }
}
