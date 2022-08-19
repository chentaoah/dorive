package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.entity.BoundedContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
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
    public int updateSelective(BoundedContext boundedContext, Object entity) {
        return proxyRepository.updateSelective(boundedContext, entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        return proxyRepository.update(boundedContext, entity);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Object example) {
        return proxyRepository.updateByExample(boundedContext, entity, example);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, Object entity) {
        return proxyRepository.insertOrUpdate(boundedContext, entity);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        return proxyRepository.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return proxyRepository.deleteByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Object example) {
        return proxyRepository.deleteByExample(boundedContext, example);
    }

}
