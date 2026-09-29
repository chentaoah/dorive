/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.factory.v1.api;

public interface TypeAdapter {

    Class<?> determineType(Object persistent);

}
