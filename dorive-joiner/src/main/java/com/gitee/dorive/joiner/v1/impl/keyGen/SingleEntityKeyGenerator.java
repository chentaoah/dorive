/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.joiner.v1.impl.keyGen;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.joiner.v1.api.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleEntityKeyGenerator implements KeyGenerator {

    private final Binder binder;

    @Override
    public String generate(Context context, Object entity) {
        Object boundValue = binder.getTargetFieldValue(context, entity);
        boundValue = binder.input(context, boundValue);
        return boundValue != null ? boundValue.toString() : null;
    }

}
