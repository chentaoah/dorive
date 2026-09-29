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

import java.util.List;

@Data
@AllArgsConstructor
public class MultiFieldKeyGenerator implements KeyGenerator {

    private final List<Binder> binders;

    @Override
    public String generate(Context context, Object entity) {
        StringBuilder keyBuilder = new StringBuilder();
        for (Binder binder : binders) {
            Object fieldValue = binder.getSourceFieldValue(context, entity);
            if (fieldValue != null) {
                String key = fieldValue.toString();
                keyBuilder.append("(").append(key.length()).append(")").append(key).append(",");
            } else {
                keyBuilder = null;
                break;
            }
        }
        if (keyBuilder != null && !keyBuilder.isEmpty()) {
            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
            return keyBuilder.toString();
        }
        return null;
    }

}
