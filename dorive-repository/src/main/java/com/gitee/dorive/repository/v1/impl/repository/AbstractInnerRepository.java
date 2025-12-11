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

package com.gitee.dorive.repository.v1.impl.repository;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.ctx.DefaultContext;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.InnerExample;
import com.gitee.dorive.base.v1.core.entity.qry.Page;
import com.gitee.dorive.base.v1.core.util.ExampleUtils;

import java.util.List;

public abstract class AbstractInnerRepository<E, PK> extends AbstractQueryRepository<E, PK> {

    @Override
    public E selectByPrimaryKey(Options options, PK primaryKey) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.selectByPrimaryKey(options, primaryKey);
    }

    @Override
    public List<E> selectByExample(Options options, Example example) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.clone(example);
        }
        return super.selectByExample(options, example);
    }

    @Override
    public E selectOneByExample(Options options, Example example) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.clone(example);
        }
        return super.selectOneByExample(options, example);
    }

    @Override
    public Page<E> selectPageByExample(Options options, Example example) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.clone(example);
        }
        return super.selectPageByExample(options, example);
    }

    @Override
    public long selectCountByExample(Options options, Example example) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.clone(example);
        }
        return super.selectCountByExample(options, example);
    }

    @Override
    public int insert(Options options, E entity) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.insert(options, entity);
    }

    @Override
    public int update(Options options, E entity) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.update(options, entity);
    }

    @Override
    public int updateByExample(Options options, Object entity, Example example) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.clone(example);
        }
        return super.updateByExample(options, entity, example);
    }

    @Override
    public int insertOrUpdate(Options options, E entity) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.insertOrUpdate(options, entity);
    }

    @Override
    public int delete(Options options, E entity) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.delete(options, entity);
    }

    @Override
    public int deleteByPrimaryKey(Options options, PK primaryKey) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.deleteByPrimaryKey(options, primaryKey);
    }

    @Override
    public int deleteByExample(Options options, Example example) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.clone(example);
        }
        return super.deleteByExample(options, example);
    }

    @Override
    public int insertList(Options options, List<E> entities) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.insertList(options, entities);
    }

    @Override
    public int updateList(Options options, List<E> entities) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.updateList(options, entities);
    }

    @Override
    public int insertOrUpdateList(Options options, List<E> entities) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.insertOrUpdateList(options, entities);
    }

    @Override
    public int deleteList(Options options, List<E> entities) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.deleteList(options, entities);
    }

    @Override
    public List<E> selectByQuery(Options options, Object query) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.selectByQuery(options, query);
    }

    @Override
    public Page<E> selectPageByQuery(Options options, Object query) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.selectPageByQuery(options, query);
    }

    @Override
    public long selectCountByQuery(Options options, Object query) {
        if (!(options instanceof Context)) {
            options = new DefaultContext(options);
        }
        return super.selectCountByQuery(options, query);
    }

}
