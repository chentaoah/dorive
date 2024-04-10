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

package com.gitee.dorive.mybatis.plus.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.dorive.api.constant.Order;
import com.gitee.dorive.api.def.EntityDef;
import com.gitee.dorive.api.entity.EntityEle;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.entity.common.EntityStoreInfo;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.operation.ext.Delete;
import com.gitee.dorive.core.entity.operation.ext.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.entity.operation.ext.Update;
import com.gitee.dorive.core.impl.executor.AbstractExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
public class MybatisPlusExecutor extends AbstractExecutor {

    private EntityDef entityDef;
    private EntityEle entityEle;
    private EntityStoreInfo entityStoreInfo;
    private BaseMapper<Object> baseMapper;
    private Class<Object> pojoClass;

    @SuppressWarnings("unchecked")
    public MybatisPlusExecutor(EntityDef entityDef, EntityEle entityEle, EntityStoreInfo entityStoreInfo) {
        this.entityDef = entityDef;
        this.entityEle = entityEle;
        this.entityStoreInfo = entityStoreInfo;
        this.baseMapper = (BaseMapper<Object>) entityStoreInfo.getMapper();
        this.pojoClass = (Class<Object>) entityStoreInfo.getPojoClass();
    }

    @Override
    public Result<Object> executeQuery(Context context, Query query) {
        Object primaryKey = query.getPrimaryKey();
        if (primaryKey != null) {
            QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(entityStoreInfo.getIdColumn(), primaryKey);
            List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
            return new Result<>(null, resultMaps);
        }

        Example example = query.getExample();
        if (example != null) {
            QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);
            com.gitee.dorive.core.entity.executor.Page<Object> page = example.getPage();
            if (page != null) {
                Page<Map<String, Object>> queryPage = baseMapper.selectMapsPage(new Page<>(page.getCurrent(), page.getSize()), queryWrapper);
                page.setTotal(queryPage.getTotal());
                return new Result<>(page, queryPage.getRecords());

            } else {
                List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
                return new Result<>(null, resultMaps);
            }
        }

        return new Result<>(null, Collections.emptyList());
    }

    @Override
    public long executeCount(Context context, Query query) {
        Example example = query.getExample();
        if (example != null) {
            QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);
            return baseMapper.selectCount(queryWrapper);
        }
        return 0L;
    }

    private QueryWrapper<Object> buildQueryWrapper(Example example) {
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();

        List<String> selectProps = example.getSelectProps();
        if (selectProps != null && !selectProps.isEmpty()) {
            queryWrapper.select(selectProps);
        }

        String selectSuffix = example.getSelectSuffix();
        if (StringUtils.isNotBlank(selectSuffix)) {
            String sqlSelect = queryWrapper.getSqlSelect();
            if (StringUtils.isBlank(sqlSelect)) {
                sqlSelect = queryWrapper.select(pojoClass, i -> true).getSqlSelect();
            }
            queryWrapper.select(sqlSelect + StringPool.COMMA + selectSuffix);
        }

        AppenderContext.appendCriterion(queryWrapper, example);

        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            String order = orderBy.getOrder();
            if (Order.ASC.equals(order)) {
                queryWrapper.orderByAsc(orderBy.getProperties());

            } else if (Order.DESC.equals(order)) {
                queryWrapper.orderByDesc(orderBy.getProperties());
            }
        }

        return queryWrapper;
    }

    @Override
    public int execute(Context context, Operation operation) {
        Object persistent = operation.getEntity();

        if (operation instanceof Insert) {
            return baseMapper.insert(persistent);

        } else if (operation instanceof Update) {
            Update update = (Update) operation;
            Object primaryKey = update.getPrimaryKey();
            Example example = update.getExample();

            Set<String> nullableProps = update.getNullableProps();
            if (nullableProps != null && !nullableProps.isEmpty()) {
                UpdateWrapper<Object> updateWrapper = buildUpdateWrapper(persistent, nullableProps, primaryKey, example);
                return baseMapper.update(null, updateWrapper);
            }

            if (primaryKey != null) {
                return baseMapper.updateById(persistent);

            } else if (example != null) {
                return baseMapper.update(persistent, buildUpdateWrapper(example));
            }

        } else if (operation instanceof Delete) {
            Delete delete = (Delete) operation;
            Object primaryKey = delete.getPrimaryKey();
            Example example = delete.getExample();

            if (primaryKey != null) {
                return baseMapper.deleteById((Serializable) primaryKey);

            } else if (example != null) {
                return baseMapper.delete(buildUpdateWrapper(example));
            }
        }
        return 0;
    }

    private UpdateWrapper<Object> buildUpdateWrapper(Example example) {
        UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();
        AppenderContext.appendCriterion(updateWrapper, example);
        return updateWrapper;
    }

    private UpdateWrapper<Object> buildUpdateWrapper(Object persistent, Set<String> nullableProps, Object primaryKey, Example example) {
        UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();

        Map<String, String> propAliasMappingWithoutPk = entityStoreInfo.getPropAliasMappingWithoutPk();
        propAliasMappingWithoutPk.forEach((prop, alias) -> {
            Object value = BeanUtil.getFieldValue(persistent, prop);
            if (value != null || nullableProps.contains(alias)) {
                updateWrapper.set(true, alias, value);
            }
        });

        if (primaryKey != null) {
            updateWrapper.eq(entityStoreInfo.getIdColumn(), primaryKey);
        }
        if (example != null) {
            AppenderContext.appendCriterion(updateWrapper, example);
        }
        return updateWrapper;
    }

}
