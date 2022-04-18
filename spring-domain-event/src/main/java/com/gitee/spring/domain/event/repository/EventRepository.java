package com.gitee.spring.domain.event.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.repository.AbstractRepository;
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

    public EventRepository(EntityPropertyChain entityPropertyChain,
                           EntityDefinition entityDefinition,
                           EntityMapper entityMapper,
                           EntityAssembler entityAssembler,
                           AbstractRepository<Object, Object> repository,
                           ApplicationContext applicationContext) {
        super(entityPropertyChain, entityDefinition, entityMapper, entityAssembler, repository);
        this.applicationContext = applicationContext;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        repositoryEvent.setMethodName("insert");
        repositoryEvent.setOperationType(OperationType.INSERT);
        repositoryEvent.setBoundedContext(boundedContext);
        repositoryEvent.setEntity(entity);
        applicationContext.publishEvent(repositoryEvent);
        return super.insert(boundedContext, entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        repositoryEvent.setMethodName("update");
        repositoryEvent.setOperationType(OperationType.UPDATE);
        repositoryEvent.setBoundedContext(boundedContext);
        repositoryEvent.setEntity(entity);
        applicationContext.publishEvent(repositoryEvent);
        return super.update(boundedContext, entity);
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        repositoryEvent.setMethodName("updateByExample");
        repositoryEvent.setOperationType(OperationType.UPDATE);
        repositoryEvent.setEntity(entity);
        repositoryEvent.setExample(example);
        applicationContext.publishEvent(repositoryEvent);
        return super.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        repositoryEvent.setMethodName("delete");
        repositoryEvent.setOperationType(OperationType.DELETE);
        repositoryEvent.setBoundedContext(boundedContext);
        repositoryEvent.setEntity(entity);
        applicationContext.publishEvent(repositoryEvent);
        return super.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        repositoryEvent.setMethodName("deleteByPrimaryKey");
        repositoryEvent.setOperationType(OperationType.DELETE);
        repositoryEvent.setPrimaryKey(primaryKey);
        applicationContext.publishEvent(repositoryEvent);
        return super.deleteByPrimaryKey(primaryKey);
    }

    @Override
    public int deleteByExample(Object example) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        repositoryEvent.setMethodName("deleteByExample");
        repositoryEvent.setOperationType(OperationType.DELETE);
        repositoryEvent.setExample(example);
        applicationContext.publishEvent(repositoryEvent);
        return super.deleteByExample(example);
    }

}
