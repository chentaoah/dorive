package com.gitee.spring.domain.core3.repository;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.entity.executor.Page;
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

    public AbstractRepository<Object, Object> getProxyRepository() {
        if (proxyRepository instanceof ProxyRepository) {
            return ((ProxyRepository) proxyRepository).getProxyRepository();
        }
        return proxyRepository;
    }

    public void setProxyRepository(AbstractRepository<Object, Object> proxyRepository) {
        if (this.proxyRepository instanceof ProxyRepository) {
            ((ProxyRepository) this.proxyRepository).setProxyRepository(proxyRepository);
        }
        this.proxyRepository = proxyRepository;
    }

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return proxyRepository.selectByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Example example) {
        return proxyRepository.selectByExample(boundedContext, example);
    }

    @Override
    public Page<Object> selectPageByExample(BoundedContext boundedContext, Example example) {
        return proxyRepository.selectPageByExample(boundedContext, example);
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
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
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
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        return proxyRepository.deleteByExample(boundedContext, example);
    }

}
