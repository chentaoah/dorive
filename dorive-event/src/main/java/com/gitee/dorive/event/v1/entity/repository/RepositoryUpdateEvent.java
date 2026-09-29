/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.event.v1.entity.repository;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUpdateEvent<T> extends RepositoryEvent<T> {
}
