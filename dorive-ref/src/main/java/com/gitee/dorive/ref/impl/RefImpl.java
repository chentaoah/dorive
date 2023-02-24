package com.gitee.dorive.ref.impl;

import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.AbstractRepository;
import com.gitee.dorive.core.repository.ProxyRepository;
import com.gitee.dorive.ref.api.Ref;
import com.gitee.dorive.ref.api.RefObj;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RefImpl extends ProxyRepository implements Ref {

    private EntityHandler entityHandler;
    private CoatingRepository<Object, Object> repository;

    @SuppressWarnings("unchecked")
    public RefImpl(AbstractRepository<Object, Object> repository, EntityHandler entityHandler) {
        super(repository);
        this.entityHandler = entityHandler;
        if (repository instanceof CoatingRepository) {
            this.repository = (CoatingRepository<Object, Object>) repository;
        }
    }

    @Override
    public List<Object> selectByCoating(BoundedContext boundedContext, Object coatingObject) {
        return repository.selectByCoating(boundedContext, coatingObject);
    }

    @Override
    public Page<Object> selectPageByCoating(BoundedContext boundedContext, Object coatingObject) {
        return repository.selectPageByCoating(boundedContext, coatingObject);
    }

    @Override
    public RefObj forObj(Object object) {
        return new RefObjImpl(this, object);
    }

}
