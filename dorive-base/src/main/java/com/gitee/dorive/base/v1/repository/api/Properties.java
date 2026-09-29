/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.repository.api;

public interface Properties {

    <T> void setProperty(Class<T> type, T instance);

    <T> T getProperty(Class<T> type);

    <T> T tryGetProperty(Class<T> type);

}
