package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.entity.BoundedContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProxyRepository extends AbstractRepository<Object, Object> {

    protected AbstractRepository<Object, Object> proxyRepository;

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return proxyRepository.selectByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        return proxyRepository.selectByExample(boundedContext, example);
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        return proxyRepository.selectPageByExample(boundedContext, example, page);
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        return proxyRepository.insert(boundedContext, entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        return proxyRepository.update(boundedContext, entity);
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        return proxyRepository.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        return proxyRepository.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        return proxyRepository.deleteByPrimaryKey(primaryKey);
    }

    @Override
    public int deleteByExample(Object example) {
        return proxyRepository.deleteByExample(example);
    }

}
