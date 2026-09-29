/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.joiner.v1.api;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface CollectionJoiner {

    <S, T> void joinAndSet(List<S> entities1, Function<S, String> keyGen1,
                           List<T> entities2, Function<T, String> keyGen2,
                           boolean collection, BiConsumer<S, Object> setter);

}