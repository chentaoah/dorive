/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.event.v1.entity.repository;

import com.gitee.dorive.event.v1.entity.BaseEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryEvent<T> extends BaseEvent<T> {
}
