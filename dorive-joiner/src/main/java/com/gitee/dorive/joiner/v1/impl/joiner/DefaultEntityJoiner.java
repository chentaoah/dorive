/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.joiner.v1.impl.joiner;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.binder.api.BinderExecutor;
import com.gitee.dorive.base.v1.definition.entity.EntityElement;
import com.gitee.dorive.base.v1.binder.enums.JoinType;
import com.gitee.dorive.base.v1.executor.api.Context;
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
    private RepositoryItem repositoryItem;
    private KeyGenerator keyGen1;
    private KeyGenerator keyGen2;
    private BiConsumer<Object, Object> setter;

    public DefaultEntityJoiner(RepositoryItem repositoryItem) {
        this.repositoryItem = repositoryItem;
        EntityElement entityElement = repositoryItem.getEntityElement();
        BinderExecutor binderExecutor = repositoryItem.getBinderExecutor();
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
                repositoryItem.isCollection(), setter);
    }
}
