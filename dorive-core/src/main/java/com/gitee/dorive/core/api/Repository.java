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
package com.gitee.dorive.core.api;

import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.BoundedContext;

import java.util.List;

/**
 * 仓储接口
 *
 * @param <E>  实体类型
 * @param <PK> 主键类型
 */
public interface Repository<E, PK> {

    /**
     * 根据主键查询实体
     *
     * @param boundedContext 边界上下文
     * @param primaryKey     主键
     * @return 实体
     */
    E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    /**
     * 根据条件查询实体
     *
     * @param boundedContext 边界上下文
     * @param example        条件
     * @return 实体
     */
    List<E> selectByExample(BoundedContext boundedContext, Example example);

    /**
     * 根据条件查询分页
     *
     * @param boundedContext 边界上下文
     * @param example        条件
     * @return 分页
     */
    Page<E> selectPageByExample(BoundedContext boundedContext, Example example);

    /**
     * 根据条件查询结果集
     *
     * @param boundedContext 边界上下文
     * @param example        条件
     * @return 结果集
     */
    Result<E> selectResultByExample(BoundedContext boundedContext, Example example);

    /**
     * 插入一个实体
     *
     * @param boundedContext 边界上下文
     * @param entity         实体
     * @return 操作数
     */
    int insert(BoundedContext boundedContext, E entity);

    /**
     * 根据实体的主键，修改一个实体
     *
     * @param boundedContext 边界上下文
     * @param entity         实体
     * @return 操作数
     */
    int update(BoundedContext boundedContext, E entity);

    /**
     * 根据实体和条件，修改聚合内的所有实体
     *
     * @param boundedContext 边界上下文
     * @param entity         实体
     * @param example        条件
     * @return 操作数
     */
    int updateByExample(BoundedContext boundedContext, Object entity, Example example);

    /**
     * 根据实体的主键，插入或者修改一个实体。
     * 主键为空则插入，主键不为空则修改。
     *
     * @param boundedContext 边界上下文
     * @param entity         实体
     * @return 操作数
     */
    int insertOrUpdate(BoundedContext boundedContext, E entity);

    /**
     * 根据实体的主键，删除一个实体
     *
     * @param boundedContext 边界上下文
     * @param entity         实体
     * @return 操作数
     */
    int delete(BoundedContext boundedContext, E entity);

    /**
     * 根据主键，删除一个实体
     *
     * @param boundedContext 边界上下文
     * @param primaryKey     主键
     * @return 操作数
     */
    int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey);

    /**
     * 根据条件，删除聚合内的所有实体
     *
     * @param boundedContext 边界上下文
     * @param example        条件
     * @return 操作数
     */
    int deleteByExample(BoundedContext boundedContext, Example example);

}
