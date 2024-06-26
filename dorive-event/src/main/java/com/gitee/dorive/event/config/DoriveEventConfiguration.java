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

package com.gitee.dorive.event.config;

import com.gitee.dorive.event.listener.ExecutorBatchEventListener;
import com.gitee.dorive.event.listener.ExecutorEventListener;
import com.gitee.dorive.event.listener.RepositoryEventListener;
import com.gitee.dorive.event.listener.RepositoryRootEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(-100)
@Configuration
public class DoriveEventConfiguration {

    @Bean("executorEventListenerV3")
    public ExecutorEventListener executorEventListener() {
        return new ExecutorEventListener();
    }

    @Bean("executorBatchEventListenerV3")
    public ExecutorBatchEventListener executorBatchEventListener() {
        return new ExecutorBatchEventListener();
    }

    @Bean("repositoryEventListenerV3")
    public RepositoryEventListener repositoryEventListener() {
        return new RepositoryEventListener();
    }

    @Bean("repositoryRootEventListenerV3")
    public RepositoryRootEventListener repositoryRootEventListener() {
        return new RepositoryRootEventListener();
    }

}
