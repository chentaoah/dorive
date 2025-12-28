/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.base.v1.core.api;

import com.gitee.dorive.base.v1.core.entity.ctx.EnumOptions;
import com.gitee.dorive.base.v1.repository.api.RepositoryMatcher;
import com.gitee.dorive.base.v1.repository.impl.matcher.AllRepositoryMatcher;
import com.gitee.dorive.base.v1.repository.impl.matcher.NoneRepositoryMatcher;
import com.gitee.dorive.base.v1.repository.impl.matcher.RootRepositoryMatcher;

import java.util.Map;

public interface Options {

    Options NONE = new EnumOptions(RepositoryMatcher.class, new NoneRepositoryMatcher());
    Options ROOT = new EnumOptions(RepositoryMatcher.class, new RootRepositoryMatcher());
    Options ALL = new EnumOptions(RepositoryMatcher.class, new AllRepositoryMatcher());

    Map<Class<?>, Object> getOptions();

    <T> void setOption(Class<T> type, T value);

    <T> T getOption(Class<T> type);

    void removeOption(Class<?> type);

}
