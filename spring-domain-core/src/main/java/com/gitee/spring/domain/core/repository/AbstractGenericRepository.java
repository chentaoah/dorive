package com.gitee.spring.domain.core.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.ListableRepository;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.Example;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractGenericRepository<E, PK> extends AbstractContextRepository<E, PK> implements ListableRepository<E, PK> {

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        Assert.notNull(entity, "The entity cannot be null!");
        int totalCount = 0;
        for (ConfiguredRepository repository : getOrderedRepositories()) {
            if (repository.matchContext(boundedContext)) {
                totalCount += repository.updateByExample(boundedContext, entity, example);
            }
        }
        return totalCount;
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        E entity = selectByPrimaryKey(boundedContext, primaryKey);
        return delete(boundedContext, entity);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        int totalCount = 0;
        for (ConfiguredRepository repository : getOrderedRepositories()) {
            if (repository.matchContext(boundedContext)) {
                totalCount += repository.deleteByExample(boundedContext, example);
            }
        }
        return totalCount;
    }

    @Override
    public int insertList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insert(boundedContext, entity)).sum();
    }

    @Override
    public int updateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> update(boundedContext, entity)).sum();
    }

    @Override
    public int insertOrUpdateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insertOrUpdate(boundedContext, entity)).sum();
    }

    @Override
    public int deleteList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> delete(boundedContext, entity)).sum();
    }

}
