package com.gitee.dorive.ref.impl;

import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.ListableRepository;
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
public class RefImpl extends ProxyRepository implements Ref<Object> {

    private EntityHandler entityHandler;
    private ListableRepository<Object, Object> listableRepository;
    private CoatingRepository<Object, Object> coatingRepository;

    @SuppressWarnings("unchecked")
    public RefImpl(AbstractRepository<Object, Object> repository, EntityHandler entityHandler) {
        super(repository);
        this.entityHandler = entityHandler;
        if (repository instanceof ListableRepository) {
            this.listableRepository = (ListableRepository<Object, Object>) repository;
        }
        if (repository instanceof CoatingRepository) {
            this.coatingRepository = (CoatingRepository<Object, Object>) repository;
        }
    }

    @Override
    public int insertList(Context context, List<Object> entities) {
        return listableRepository.insertList(context, entities);
    }

    @Override
    public int updateList(Context context, List<Object> entities) {
        return listableRepository.updateList(context, entities);
    }

    @Override
    public int insertOrUpdateList(Context context, List<Object> entities) {
        return listableRepository.insertOrUpdateList(context, entities);
    }

    @Override
    public int deleteList(Context context, List<Object> entities) {
        return listableRepository.deleteList(context, entities);
    }

    @Override
    public List<Object> selectByCoating(Context context, Object coatingObject) {
        return coatingRepository.selectByCoating(context, coatingObject);
    }

    @Override
    public Page<Object> selectPageByCoating(Context context, Object coatingObject) {
        return coatingRepository.selectPageByCoating(context, coatingObject);
    }

    @Override
    public RefObj forObj(Object object) {
        return new RefObjImpl(this, object);
    }

}
