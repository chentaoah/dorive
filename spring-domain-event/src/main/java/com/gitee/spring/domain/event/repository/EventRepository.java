package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.event.entity.OperationType;
import com.gitee.spring.domain.event.entity.RepositoryEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

@Getter
@Setter
public class EventRepository extends ConfiguredRepository {

    protected ApplicationContext applicationContext;

    public EventRepository(ConfiguredRepository configuredRepository, ApplicationContext applicationContext) {
        super(configuredRepository);
        this.applicationContext = applicationContext;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        int count = super.insert(boundedContext, entity);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("insert");
            repositoryEvent.setOperationType(OperationType.INSERT);
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setEntity(entity);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        int count = super.update(boundedContext, entity);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("update");
            repositoryEvent.setOperationType(OperationType.UPDATE);
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setEntity(entity);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        int count = super.updateByExample(entity, example);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("updateByExample");
            repositoryEvent.setOperationType(OperationType.UPDATE);
            repositoryEvent.setEntity(entity);
            repositoryEvent.setExample(example);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        int count = super.delete(boundedContext, entity);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("delete");
            repositoryEvent.setOperationType(OperationType.DELETE);
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setEntity(entity);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        int count = super.deleteByPrimaryKey(primaryKey);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("deleteByPrimaryKey");
            repositoryEvent.setOperationType(OperationType.DELETE);
            repositoryEvent.setPrimaryKey(primaryKey);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int deleteByExample(Object example) {
        int count = super.deleteByExample(example);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("deleteByExample");
            repositoryEvent.setOperationType(OperationType.DELETE);
            repositoryEvent.setExample(example);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

}
