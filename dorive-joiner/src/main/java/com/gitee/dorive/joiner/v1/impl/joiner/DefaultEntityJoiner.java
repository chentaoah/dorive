package com.gitee.dorive.joiner.v1.impl.joiner;

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
        if (joinType == JoinType.SINGLE) {
            this.keyGen1 = new SingleEntityKeyGenerator(binderExecutor.getRootStrongBinders().get(0));
            this.keyGen2 = new SingleFieldKeyGenerator(binderExecutor.getRootStrongBinders().get(0));

        } else if (joinType == JoinType.MULTI) {
            this.keyGen1 = new MultiEntityKeyGenerator(binderExecutor.getRootStrongBinders());
            this.keyGen2 = new MultiFieldKeyGenerator(binderExecutor.getRootStrongBinders());
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
