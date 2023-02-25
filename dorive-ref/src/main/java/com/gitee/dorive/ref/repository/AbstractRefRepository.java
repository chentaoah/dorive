package com.gitee.dorive.ref.repository;

import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.repository.AbstractContextRepository;
import com.gitee.dorive.ref.impl.RefInjector;

import java.lang.reflect.Field;

public abstract class AbstractRefRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    @Override
    protected void postProcessEntityClass(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler, Class<?> entityClass) {
        RefInjector refInjector = new RefInjector(repository, entityHandler, entityClass);
        Field field = refInjector.getField();
        if (field != null) {
            refInjector.inject(field, refInjector.createRef());
        }
    }

}
