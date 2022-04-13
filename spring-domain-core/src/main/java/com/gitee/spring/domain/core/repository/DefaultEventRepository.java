package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.EntityAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.*;
import org.springframework.context.ApplicationContext;

public class DefaultEventRepository extends DefaultRepository {

    protected ApplicationContext applicationContext;

    public DefaultEventRepository(ApplicationContext applicationContext,
                                  EntityPropertyChain entityPropertyChain,
                                  EntityDefinition entityDefinition,
                                  EntityMapper entityMapper,
                                  EntityAssembler entityAssembler) {
        super(entityPropertyChain, entityDefinition, entityMapper, entityAssembler);
        this.applicationContext = applicationContext;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        EntityEvent entityEvent = new EntityEvent();
        entityEvent.setMethodName("insert");
        entityEvent.setOperationType(OperationType.INSERT);
        entityEvent.setBoundedContext(boundedContext);
        entityEvent.setEntity(entity);
        repositoryEvent.setEntityEvent(entityEvent);
        applicationContext.publishEvent(repositoryEvent);
        return super.insert(boundedContext, entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        EntityEvent entityEvent = new EntityEvent();
        entityEvent.setMethodName("update");
        entityEvent.setOperationType(OperationType.UPDATE);
        entityEvent.setBoundedContext(boundedContext);
        entityEvent.setEntity(entity);
        repositoryEvent.setEntityEvent(entityEvent);
        applicationContext.publishEvent(repositoryEvent);
        return super.update(boundedContext, entity);
    }

    @Override
    public int updateByExample(Object entity, Object example) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        EntityEvent entityEvent = new EntityEvent();
        entityEvent.setMethodName("updateByExample");
        entityEvent.setOperationType(OperationType.UPDATE);
        entityEvent.setEntity(entity);
        entityEvent.setExample(example);
        repositoryEvent.setEntityEvent(entityEvent);
        applicationContext.publishEvent(repositoryEvent);
        return super.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        EntityEvent entityEvent = new EntityEvent();
        entityEvent.setMethodName("delete");
        entityEvent.setOperationType(OperationType.DELETE);
        entityEvent.setBoundedContext(boundedContext);
        entityEvent.setEntity(entity);
        repositoryEvent.setEntityEvent(entityEvent);
        applicationContext.publishEvent(repositoryEvent);
        return super.delete(boundedContext, entity);
    }

    @Override
    public int deleteByPrimaryKey(Object primaryKey) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        EntityEvent entityEvent = new EntityEvent();
        entityEvent.setMethodName("deleteByPrimaryKey");
        entityEvent.setOperationType(OperationType.DELETE);
        entityEvent.setPrimaryKey(primaryKey);
        repositoryEvent.setEntityEvent(entityEvent);
        applicationContext.publishEvent(repositoryEvent);
        return super.deleteByPrimaryKey(primaryKey);
    }

    @Override
    public int deleteByExample(Object example) {
        RepositoryEvent repositoryEvent = new RepositoryEvent(this);
        EntityEvent entityEvent = new EntityEvent();
        entityEvent.setMethodName("deleteByExample");
        entityEvent.setOperationType(OperationType.DELETE);
        entityEvent.setExample(example);
        repositoryEvent.setEntityEvent(entityEvent);
        applicationContext.publishEvent(repositoryEvent);
        return super.deleteByExample(example);
    }

}
