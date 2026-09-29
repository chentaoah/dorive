/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;

public interface Ref<E> extends QueryRepository<E, Object>, ListableRepository<E, Object> {

    <R extends AbstractQueryRepository<?, ?>> R get();

    RefObj forObj(E obj);

}
