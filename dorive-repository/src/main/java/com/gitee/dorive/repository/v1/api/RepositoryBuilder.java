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

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.core.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;

public interface RepositoryBuilder {

    BinderExecutor newBinderExecutor(RepositoryContext repositoryContext, EntityElement entityElement);

    Matcher newAdaptiveMatcher(boolean root, String name);

    EntityHandler newEntityHandler(RepositoryContext repositoryContext);

    EntityOpHandler newEntityOpHandler(RepositoryContext repositoryContext);

}
