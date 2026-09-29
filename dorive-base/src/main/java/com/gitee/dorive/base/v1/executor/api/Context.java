/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.api;

import java.util.Map;

public interface Context extends Options {

    Map<String, Object> getAttachments();

    void setAttachment(String name, Object value);

    Object getAttachment(String name);

    void removeAttachment(String name);

}
