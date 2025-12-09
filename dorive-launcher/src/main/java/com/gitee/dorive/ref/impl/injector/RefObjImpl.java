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

package com.gitee.dorive.ref.impl.injector;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.executor.v1.api.EntityHandler;
import com.gitee.dorive.base.v1.core.entity.ctx.DefaultContext;
import com.gitee.dorive.repository.v1.impl.repository.AbstractRepository;
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
    public long select(Options options) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        EntityHandler entityHandler = ref.getEntityHandler();
        return entityHandler.handle((Context) options, Collections.singletonList(object));
    }

    @Override
    public int insertOrUpdate(Options options) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.insertOrUpdate(options, object);
    }

    @Override
    public int delete(Options options) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        AbstractRepository<Object, Object> repository = ref.getProxyRepository();
        return repository.delete(options, object);
    }

}
