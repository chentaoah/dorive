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

package com.gitee.dorive.event.repository;

import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.repository.AbstractGenericRepository;
import com.gitee.dorive.core.repository.AbstractProxyRepository;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.DefaultRepository;
import com.gitee.dorive.event.annotation.EnableEvent;
import com.gitee.dorive.event.impl.EventExecutor;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class AbstractEventRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    private boolean enableEvent;

    @Override
    public void afterPropertiesSet() throws Exception {
        EnableEvent enableEvent = AnnotationUtils.getAnnotation(this.getClass(), EnableEvent.class);
        this.enableEvent = enableEvent != null;
        super.afterPropertiesSet();
    }

    @Override
    protected AbstractRepository<Object, Object> processRepository(AbstractRepository<Object, Object> repository) {
        if (enableEvent) {
            AbstractRepository<Object, Object> actualRepository = repository;
            if (repository instanceof AbstractProxyRepository) {
                actualRepository = ((AbstractProxyRepository) repository).getProxyRepository();
            }
            if (actualRepository instanceof DefaultRepository) {
                DefaultRepository defaultRepository = (DefaultRepository) actualRepository;
                EntityEle entityEle = defaultRepository.getEntityEle();
                Executor executor = defaultRepository.getExecutor();
                executor = new EventExecutor(executor, getApplicationContext(), entityEle);
                defaultRepository.setExecutor(executor);
            }
        }
        return repository;
    }

}
