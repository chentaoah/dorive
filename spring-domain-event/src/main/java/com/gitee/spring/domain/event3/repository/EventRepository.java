package com.gitee.spring.domain.event3.repository;

import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.entity.operation.Condition;
import com.gitee.spring.domain.core3.entity.operation.Operation;
import com.gitee.spring.domain.core3.repository.ProxyRepository;
import com.gitee.spring.domain.event3.listener.RepositoryEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

@Getter
@Setter
public class EventRepository extends ProxyRepository {

    protected ApplicationContext applicationContext;

    public EventRepository(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        int count = super.insert(boundedContext, entity);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("insert");
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setCondition(new Condition(Operation.INSERT, entity));
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
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setCondition(new Condition(Operation.UPDATE, entity));
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Example example) {
        int count = super.updateByExample(boundedContext, entity, example);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("updateByExample");
            repositoryEvent.setBoundedContext(boundedContext);
            Condition condition = new Condition(Operation.UPDATE, entity);
            condition.setExample(example);
            repositoryEvent.setCondition(condition);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, Object entity) {
        int count = super.insertOrUpdate(boundedContext, entity);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("insertOrUpdate");
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setCondition(new Condition(Operation.INSERT_OR_UPDATE, entity));
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
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setCondition(new Condition(Operation.DELETE, entity));
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        int count = super.deleteByPrimaryKey(boundedContext, primaryKey);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("deleteByPrimaryKey");
            repositoryEvent.setBoundedContext(boundedContext);
            Condition condition = new Condition(Operation.DELETE, null);
            condition.setPrimaryKey(primaryKey);
            repositoryEvent.setCondition(condition);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Example example) {
        int count = super.deleteByExample(boundedContext, example);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("deleteByExample");
            repositoryEvent.setBoundedContext(boundedContext);
            Condition condition = new Condition(Operation.DELETE, null);
            condition.setExample(example);
            repositoryEvent.setCondition(condition);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

}
