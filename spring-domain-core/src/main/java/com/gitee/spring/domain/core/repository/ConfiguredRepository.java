package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ConfiguredRepository extends AbstractRepository<Object, Object> {

    protected EntityPropertyChain entityPropertyChain;
    protected EntityDefinition entityDefinition;
    protected EntityMapper entityMapper;
    protected EntityAssembler entityAssembler;
    protected AbstractRepository<Object, Object> repository;

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return repository.selectByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        return repository.selectByExample(boundedContext, example);
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        return repository.selectPageByExample(boundedContext, example, page);
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
    public int updateByExample(Object entity, Object example) {
        return repository.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        return repository.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        return repository.deleteByPrimaryKey(primaryKey);
    }

    @Override
    public int deleteByExample(Object example) {
        return repository.deleteByExample(example);
    }

}
