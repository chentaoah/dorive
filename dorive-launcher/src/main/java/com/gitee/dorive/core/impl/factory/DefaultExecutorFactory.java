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

package com.gitee.dorive.core.impl.factory;

import com.gitee.dorive.base.v1.executor.api.EntityHandler;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandler;
import com.gitee.dorive.base.v1.executor.api.Executor;
import com.gitee.dorive.base.v1.executor.api.ExecutorFactory;
import com.gitee.dorive.base.v1.repository.api.RepositoryContext;
import com.gitee.dorive.executor.v1.impl.executor.ContextExecutor;

public class DefaultExecutorFactory implements ExecutorFactory {

    @Override
    public Executor create(String name, Object... args) {
        if ("ContextExecutor".equals(name)) {
            return new ContextExecutor((RepositoryContext) args[0], (EntityHandler) args[1], (EntityOpHandler) args[2]);
        }
        return null;
    }

}
