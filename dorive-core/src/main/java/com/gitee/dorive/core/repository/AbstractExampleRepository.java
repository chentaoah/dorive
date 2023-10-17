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

package com.gitee.dorive.core.repository;

import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.InnerExample;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.util.ExampleUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractExampleRepository<E, PK> extends AbstractGenericRepository<E, PK> {

    @Override
    public List<E> selectByExample(Context context, Example example) {
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.tryClone(example);
        }
        return super.selectByExample(context, example);
    }

    @Override
    public Page<E> selectPageByExample(Context context, Example example) {
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.tryClone(example);
        }
        return super.selectPageByExample(context, example);
    }

    @Override
    public long selectCount(Context context, Example example) {
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.tryClone(example);
        }
        return super.selectCount(context, example);
    }

    @Override
    public int updateByExample(Context context, Object entity, Example example) {
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.tryClone(example);
        }
        return super.updateByExample(context, entity, example);
    }

    @Override
    public int deleteByExample(Context context, Example example) {
        if (!(example instanceof InnerExample)) {
            example = ExampleUtils.tryClone(example);
        }
        return super.deleteByExample(context, example);
    }

}