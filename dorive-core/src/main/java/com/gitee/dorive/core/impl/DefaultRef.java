package com.gitee.dorive.core.impl;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Ref;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.AbstractRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@ToString(exclude = {"rootEntities", "self"})
public class DefaultRef implements Ref {

    private AbstractRepository<Object, Object> repository;
    private EntityHandler entityHandler;
    private BoundedContext boundedContext;
    private List<Object> rootEntities;
    private Object self;

    @Override
    public int select(BoundedContext boundedContext) {
        return entityHandler.handleEntities(boundedContext, Collections.singletonList(self));
    }

    @Override
    public int select(Selector selector) {
        return select(new BoundedContext(selector));
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext) {
        return repository.insertOrUpdate(boundedContext, self);
    }

    @Override
    public int insertOrUpdate(Selector selector) {
        return insertOrUpdate(new BoundedContext(selector));
    }

    @Override
    public int delete(BoundedContext boundedContext) {
        return repository.delete(boundedContext, self);
    }

    @Override
    public int delete(Selector selector) {
        return delete(new BoundedContext(selector));
    }

}
