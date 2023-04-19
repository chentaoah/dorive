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

package com.gitee.dorive.simple.repository;

import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.api.executor.EntityHandler;
import com.gitee.dorive.core.entity.context.InnerContext;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.simple.impl.RefInjector;

import java.lang.reflect.Field;
import java.util.List;

public abstract class AbstractRefRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    @Override
    protected void processEntityClass(EntityHandler entityHandler) {
        RefInjector refInjector = new RefInjector(this, entityHandler, getEntityClass());
        Field field = refInjector.getField();
        if (field != null) {
            refInjector.inject(field, refInjector.createRef());
        }
    }

    @Override
    public E selectByPrimaryKey(Context context, PK primaryKey) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.selectByPrimaryKey(context, primaryKey);
    }

    @Override
    public List<E> selectByExample(Context context, Example example) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.selectByExample(context, example);
    }

    @Override
    public Page<E> selectPageByExample(Context context, Example example) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.selectPageByExample(context, example);
    }

    @Override
    public long selectCountByExample(Context context, Example example) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.selectCountByExample(context, example);
    }

    @Override
    public int insert(Context context, E entity) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.insert(context, entity);
    }

    @Override
    public int update(Context context, E entity) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.update(context, entity);
    }

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.updateByExample(context, entity, example);
    }

    @Override
    public int insertOrUpdate(Context context, E entity) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.insertOrUpdate(context, entity);
    }

    @Override
    public int delete(Context context, E entity) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.delete(context, entity);
    }

    @Override
    public int deleteByPrimaryKey(Context context, PK primaryKey) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.deleteByPrimaryKey(context, primaryKey);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.deleteByExample(context, example);
    }

    @Override
    public int insertList(Context context, List<E> entities) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.insertList(context, entities);
    }

    @Override
    public int updateList(Context context, List<E> entities) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.updateList(context, entities);
    }

    @Override
    public int insertOrUpdateList(Context context, List<E> entities) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.insertOrUpdateList(context, entities);
    }

    @Override
    public int deleteList(Context context, List<E> entities) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.deleteList(context, entities);
    }

    @Override
    public List<E> selectByCoating(Context context, Object coating) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.selectByCoating(context, coating);
    }

    @Override
    public Page<E> selectPageByCoating(Context context, Object coating) {
        if (context instanceof Selector) {
            context = new InnerContext((Selector) context);
        }
        return super.selectPageByCoating(context, coating);
    }

}
