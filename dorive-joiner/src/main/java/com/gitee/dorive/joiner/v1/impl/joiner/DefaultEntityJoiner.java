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

package com.gitee.dorive.joiner.v1.impl.joiner;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.enums.JoinType;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.joiner.v1.api.CollectionJoiner;
import com.gitee.dorive.joiner.v1.api.KeyGenerator;
import com.gitee.dorive.joiner.v1.impl.keyGen.MultiEntityKeyGenerator;
import com.gitee.dorive.joiner.v1.impl.keyGen.MultiFieldKeyGenerator;
import com.gitee.dorive.joiner.v1.impl.keyGen.SingleEntityKeyGenerator;
import com.gitee.dorive.joiner.v1.impl.keyGen.SingleFieldKeyGenerator;
import lombok.Data;

import java.util.List;
import java.util.function.BiConsumer;

@Data
public class DefaultEntityJoiner implements EntityJoiner {
    private RepositoryItem repository;
    private KeyGenerator keyGen1;
    private KeyGenerator keyGen2;
    private BiConsumer<Object, Object> setter;

    public DefaultEntityJoiner(RepositoryItem repository) {
        this.repository = repository;
        EntityElement entityElement = repository.getEntityElement();
        BinderExecutor binderExecutor = repository.getBinderExecutor();
        JoinType joinType = binderExecutor.getJoinType();
        List<Binder> binders = binderExecutor.getRootStrongBinders();
        if (joinType == JoinType.SINGLE) {
            this.keyGen1 = new SingleEntityKeyGenerator(binders.get(0));
            this.keyGen2 = new SingleFieldKeyGenerator(binders.get(0));

        } else if (joinType == JoinType.MULTI) {
            this.keyGen1 = new MultiEntityKeyGenerator(binders);
            this.keyGen2 = new MultiFieldKeyGenerator(binders);
        }
        this.setter = (entity, object) -> {
            Object value = entityElement.getValue(entity);
            if (value == null) {
                entityElement.setValue(entity, object);
            }
        };
    }

    @Override
    public void join(Context context, List<Object> entities1, List<Object> entities2) {
        CollectionJoiner collectionJoiner = new DefaultCollectionJoiner();
        collectionJoiner.joinAndSet(
                entities1, o -> keyGen1.generate(context, o),
                entities2, o -> keyGen2.generate(context, o),
                repository.isCollection(), setter);
    }
}
