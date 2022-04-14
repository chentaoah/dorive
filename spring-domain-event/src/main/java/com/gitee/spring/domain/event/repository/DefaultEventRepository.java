package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.core.repository.ProxyRepository;
import com.gitee.spring.domain.event.entity.OperationType;
import com.gitee.spring.domain.event.entity.RepositoryEvent;
import org.springframework.context.ApplicationContext;

public class DefaultEventRepository extends ProxyRepository {

    protected ApplicationContext applicationContext;

    public DefaultEventRepository(AbstractRepository<Object, Object> repository, ApplicationContext applicationContext) {
        super(repository);
        this.applicationContext = applicationContext;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(repository);
        repositoryEvent.setMethodName("insert");
        repositoryEvent.setOperationType(OperationType.INSERT);
        repositoryEvent.setBoundedContext(boundedContext);
        repositoryEvent.setEntity(entity);
        applicationContext.publishEvent(repositoryEvent);
        return super.insert(boundedContext, entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(repository);
        repositoryEvent.setMethodName("update");
        repositoryEvent.setOperationType(OperationType.UPDATE);
        repositoryEvent.setBoundedContext(boundedContext);
        repositoryEvent.setEntity(entity);
        applicationContext.publishEvent(repositoryEvent);
        return super.update(boundedContext, entity);
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(repository);
        repositoryEvent.setMethodName("updateByExample");
        repositoryEvent.setOperationType(OperationType.UPDATE);
        repositoryEvent.setEntity(entity);
        repositoryEvent.setExample(example);
        applicationContext.publishEvent(repositoryEvent);
        return super.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(repository);
        repositoryEvent.setMethodName("delete");
        repositoryEvent.setOperationType(OperationType.DELETE);
        repositoryEvent.setBoundedContext(boundedContext);
        repositoryEvent.setEntity(entity);
        applicationContext.publishEvent(repositoryEvent);
        return super.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(repository);
        repositoryEvent.setMethodName("deleteByPrimaryKey");
        repositoryEvent.setOperationType(OperationType.DELETE);
        repositoryEvent.setPrimaryKey(primaryKey);
        applicationContext.publishEvent(repositoryEvent);
        return super.deleteByPrimaryKey(primaryKey);
    }

    @Override
    public int deleteByExample(Object example) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(repository);
        repositoryEvent.setMethodName("deleteByExample");
        repositoryEvent.setOperationType(OperationType.DELETE);
        repositoryEvent.setExample(example);
        applicationContext.publishEvent(repositoryEvent);
        return super.deleteByExample(example);
    }

}
