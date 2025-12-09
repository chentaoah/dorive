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

package com.gitee.dorive.repository.v1.api;

import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.Example;
import com.gitee.dorive.base.v1.core.entity.Page;

import java.util.List;

/**
 * 仓储接口
 *
 * @param <E>  实体类型
 * @param <PK> 主键类型
 */
public interface Repository<E, PK> {

    /**
     * 根据主键，查询实体
     *
     * @param options    可选项
     * @param primaryKey 主键
     * @return 实体
     */
    E selectByPrimaryKey(Options options, PK primaryKey);

    /**
     * 根据条件，查询实体
     *
     * @param options 可选项
     * @param example 条件
     * @return 实体
     */
    List<E> selectByExample(Options options, Example example);

    /**
     * 根据条件，查询实体
     * 如果存在多条记录，取第一条
     *
     * @param options 可选项
     * @param example 条件
     * @return 实体
     */
    E selectOneByExample(Options options, Example example);

    /**
     * 根据条件，查询分页
     *
     * @param options 可选项
     * @param example 条件
     * @return 分页
     */
    Page<E> selectPageByExample(Options options, Example example);

    /**
     * 根据条件，查询计数
     *
     * @param options 可选项
     * @param example 条件
     * @return 计数
     */
    long selectCountByExample(Options options, Example example);

    /**
     * 插入一个实体
     *
     * @param options 可选项
     * @param entity  实体
     * @return 操作数
     */
    int insert(Options options, E entity);

    /**
     * 根据实体的主键，修改一个实体
     *
     * @param options 可选项
     * @param entity  实体
     * @return 操作数
     */
    int update(Options options, E entity);

    /**
     * 根据实体和条件，修改聚合内的所有实体
     *
     * @param options 可选项
     * @param entity  实体
     * @param example 条件
     * @return 操作数
     */
    int updateByExample(Options options, Object entity, Example example);

    /**
     * 根据实体的主键，插入或者修改一个实体。
     * 主键为空则插入，主键非空则修改。
     *
     * @param options 可选项
     * @param entity  实体
     * @return 操作数
     */
    int insertOrUpdate(Options options, E entity);

    /**
     * 根据实体的主键，删除一个实体
     *
     * @param options 可选项
     * @param entity  实体
     * @return 操作数
     */
    int delete(Options options, E entity);

    /**
     * 根据主键，删除一个实体
     *
     * @param options    可选项
     * @param primaryKey 主键
     * @return 操作数
     */
    int deleteByPrimaryKey(Options options, PK primaryKey);

    /**
     * 根据条件，删除聚合内的所有实体
     *
     * @param options 可选项
     * @param example 条件
     * @return 操作数
     */
    int deleteByExample(Options options, Example example);

}
