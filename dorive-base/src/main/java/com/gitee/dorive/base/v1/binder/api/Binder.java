/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.binder.api;

import com.gitee.dorive.base.v1.executor.api.Context;

public interface Binder extends Processor {

    String getSourceField();

    String getTargetField();

    Object getSourceFieldValue(Context context, Object entity);

    void setSourceFieldValue(Context context, Object entity, Object value);

    Object getTargetFieldValue(Context context, Object entity);

    void setTargetFieldValue(Context context, Object entity, Object value);

}
