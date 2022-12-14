/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.dorive.event.repository;

import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.core.repository.ProxyRepository;
import com.gitee.dorive.event.entity.RepositoryEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationContext;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EventRepository extends ProxyRepository {

    protected ApplicationContext applicationContext;

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        int count = super.insert(boundedContext, entity);
        if (count != 0) {
            RepositoryEvent repositoryEvent = new RepositoryEvent(this);
            repositoryEvent.setMethodName("insert");
            repositoryEvent.setBoundedContext(boundedContext);
            repositoryEvent.setOperation(new Insert(Operation.INSERT, entity));
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
            repositoryEvent.setOperation(new Update(Operation.UPDATE, entity));
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
            Update update = new Update(Operation.UPDATE, entity);
            update.setExample(example);
            repositoryEvent.setOperation(update);
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
            repositoryEvent.setOperation(new Operation(Operation.INSERT_OR_UPDATE, entity));
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
            repositoryEvent.setOperation(new Delete(Operation.DELETE, entity));
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
            Delete delete = new Delete(Operation.DELETE, null);
            delete.setPrimaryKey(primaryKey);
            repositoryEvent.setOperation(delete);
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
            Delete delete = new Delete(Operation.DELETE, null);
            delete.setExample(example);
            repositoryEvent.setOperation(delete);
            applicationContext.publishEvent(repositoryEvent);
        }
        return count;
    }

}
