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

package com.gitee.dorive.ref.impl;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Options;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.context.InnerContext;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.ref.api.RefObj;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

@Data
@AllArgsConstructor
public class RefObjImpl implements RefObj {

    private RefImpl ref;
    private Object object;

    @Override
    public long select(Context context) {
        EntityHandler entityHandler = ref.getEntityHandler();
        return entityHandler.handle(context, Collections.singletonList(object));
    }

    @Override
    public int insertOrUpdate(Context context) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.insertOrUpdate(context, object);
    }

    @Override
    public int delete(Context context) {
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.delete(context, object);
    }

    @Override
    public long select(Options options) {
        return select(new InnerContext(options));
    }

    @Override
    public int insertOrUpdate(Options options) {
        return insertOrUpdate(new InnerContext(options));
    }

    @Override
    public int delete(Options options) {
        return delete(new InnerContext(options));
    }

}
