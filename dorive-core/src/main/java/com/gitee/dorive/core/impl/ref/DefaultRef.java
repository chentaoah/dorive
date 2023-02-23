package com.gitee.dorive.core.impl.ref;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Obj;
import com.gitee.dorive.core.api.Ref;
import com.gitee.dorive.core.api.Repository;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DefaultRef implements Ref {

    private Repository<Object, Object> repository;
    private EntityHandler entityHandler;

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return repository.selectByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Example example) {
        return repository.selectByExample(boundedContext, example);
    }

    @Override
    public Page<Object> selectPageByExample(BoundedContext boundedContext, Example example) {
        return repository.selectPageByExample(boundedContext, example);
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        return repository.insert(boundedContext, entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        return repository.update(boundedContext, entity);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        return repository.updateByExample(boundedContext, entity, example);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, Object entity) {
        return repository.insertOrUpdate(boundedContext, entity);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        return repository.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return repository.deleteByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        return repository.deleteByExample(boundedContext, example);
    }

    @Override
    public Obj bind(Object object) {
        return new DefaultObj(this, object);
    }

}
