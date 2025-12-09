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

package com.gitee.dorive.autoconfigure.core;

import com.gitee.dorive.base.v1.executor.api.EntityHandlerFactory;
import com.gitee.dorive.base.v1.executor.api.EntityJoinerFactory;
import com.gitee.dorive.base.v1.executor.api.EntityOpHandlerFactory;
import com.gitee.dorive.base.v1.executor.api.ExecutorFactory;
import com.gitee.dorive.core.impl.factory.*;
import com.gitee.dorive.repository.v1.api.RepositoryBuilder;
import com.gitee.dorive.repository.v1.impl.context.RepositoryContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DoriveCoreConfiguration {

    @Bean("RepositoryContextV3")
    public static RepositoryContext repositoryContext() {
        return new RepositoryContext();
    }

    @Bean("RepositoryBuilderV3")
    public static RepositoryBuilder repositoryBuilder() {
        return new DefaultRepositoryBuilder();
    }

    @Bean("ExecutorFactoryV3")
    public static ExecutorFactory executorFactory() {
        return new DefaultExecutorFactory();
    }

    @Bean("EntityHandlerFactoryV3")
    public static EntityHandlerFactory entityHandlerFactory() {
        return new DefaultEntityHandlerFactory();
    }

    @Bean("EntityJoinerFactoryV3")
    public static EntityJoinerFactory entityJoinerFactory() {
        return new DefaultEntityJoinerFactory();
    }

    @Bean("EntityOpHandlerFactoryV3")
    public static EntityOpHandlerFactory entityOpHandlerFactory() {
        return new DefaultEntityOpHandlerFactory();
    }

}
