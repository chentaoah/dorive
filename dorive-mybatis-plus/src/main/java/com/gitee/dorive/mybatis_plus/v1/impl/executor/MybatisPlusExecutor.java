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

package com.gitee.dorive.mybatis_plus.v1.impl.executor;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.dorive.base.v1.common.constant.Sort;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.common.def.EntityDef;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.core.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.core.entity.op.Result;
import com.gitee.dorive.base.v1.core.entity.op.Condition;
import com.gitee.dorive.base.v1.core.entity.op.EntityOp;
import com.gitee.dorive.base.v1.core.entity.op.Operation;
import com.gitee.dorive.base.v1.core.entity.cop.ConditionDelete;
import com.gitee.dorive.base.v1.core.entity.cop.ConditionUpdate;
import com.gitee.dorive.base.v1.core.entity.cop.Query;
import com.gitee.dorive.base.v1.core.entity.eop.Delete;
import com.gitee.dorive.base.v1.core.entity.eop.Insert;
import com.gitee.dorive.base.v1.core.entity.eop.Update;
import com.gitee.dorive.base.v1.executor.impl.AbstractExecutor;
import com.gitee.dorive.base.v1.mybatis.entity.EntityStoreInfo;
import com.gitee.dorive.mybatis_plus.v1.api.EasyBaseMapper;
import com.gitee.dorive.mybatis_plus.v1.enums.InsertMethod;
import com.gitee.dorive.mybatis_plus.v1.impl.common.AppenderContext;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@ToString
public class MybatisPlusExecutor extends AbstractExecutor {

    private EntityDef entityDef;
    private EntityElement entityElement;
    private EntityStoreInfo entityStoreInfo;
    private BaseMapper<Object> baseMapper;
    private Class<Object> pojoClass;
    private boolean canInsertBatch;

    @SuppressWarnings("unchecked")
    public MybatisPlusExecutor(EntityDef entityDef, EntityElement entityElement, EntityStoreInfo entityStoreInfo) {
        this.entityDef = entityDef;
        this.entityElement = entityElement;
        this.entityStoreInfo = entityStoreInfo;
        this.baseMapper = (BaseMapper<Object>) entityStoreInfo.getMapper();
        this.pojoClass = (Class<Object>) entityStoreInfo.getPojoClass();
        this.canInsertBatch = baseMapper instanceof EasyBaseMapper;
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
            com.gitee.dorive.base.v1.core.entity.qry.Page<Object> page = example.getPage();
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
            String sort = orderBy.getSort();
            if (Sort.ASC.equals(sort)) {
                queryWrapper.orderByAsc(orderBy.getProperties());

            } else if (Sort.DESC.equals(sort)) {
                queryWrapper.orderByDesc(orderBy.getProperties());
            }
        }

        return queryWrapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int execute(Context context, Operation operation) {
        int totalCount = 0;
        if (operation instanceof EntityOp) {
            EntityOp entityOp = (EntityOp) operation;
            List<?> persistentObjs = entityOp.getEntities();
            if (entityOp instanceof Insert) {
                InsertMethod insertMethod = context.getOption(InsertMethod.class);
                boolean isBatch = insertMethod == null ? persistentObjs.size() >= 100 : insertMethod == InsertMethod.BATCH;
                if (canInsertBatch && isBatch) {
                    totalCount += ((EasyBaseMapper<Object>) baseMapper).insertBatchSomeColumn((Collection<Object>) persistentObjs);
                } else {
                    for (Object persistentObj : persistentObjs) {
                        totalCount += baseMapper.insert(persistentObj);
                    }
                }

            } else if (operation instanceof Update) {
                Update update = (Update) operation;
                Set<String> nullableProps = update.getNullableProps();
                if (nullableProps != null && !nullableProps.isEmpty()) {
                    for (Object persistentObj : persistentObjs) {
                        Object primaryKey = BeanUtil.getFieldValue(persistentObj, entityStoreInfo.getIdProperty());
                        UpdateWrapper<Object> updateWrapper = buildUpdateWrapper(primaryKey);
                        buildUpdateWrapper(updateWrapper, persistentObj, nullableProps);
                        totalCount += baseMapper.update(null, updateWrapper);
                    }
                } else {
                    for (Object persistentObj : persistentObjs) {
                        totalCount += baseMapper.updateById(persistentObj);
                    }
                }

            } else if (operation instanceof Delete) {
                for (Object persistentObj : persistentObjs) {
                    totalCount += baseMapper.deleteById(persistentObj);
                }
            }

        } else if (operation instanceof Condition) {
            if (operation instanceof ConditionUpdate) {
                ConditionUpdate conditionUpdate = (ConditionUpdate) operation;
                Object persistentObj = conditionUpdate.getEntity();
                Set<String> nullableProps = conditionUpdate.getNullableProps();
                Object primaryKey = conditionUpdate.getPrimaryKey();
                Example example = conditionUpdate.getExample();
                UpdateWrapper<Object> updateWrapper = null;
                // 设置查询条件
                if (primaryKey != null) {
                    updateWrapper = buildUpdateWrapper(primaryKey);

                } else if (example != null) {
                    updateWrapper = buildUpdateWrapper(example);
                }
                if (updateWrapper != null) {
                    if (nullableProps != null && !nullableProps.isEmpty()) {
                        // 设置更新的值
                        buildUpdateWrapper(updateWrapper, persistentObj, nullableProps);
                        totalCount += baseMapper.update(null, updateWrapper);
                    } else {
                        totalCount += baseMapper.update(persistentObj, updateWrapper);
                    }
                }

            } else if (operation instanceof ConditionDelete) {
                ConditionDelete conditionDelete = (ConditionDelete) operation;
                Object primaryKey = conditionDelete.getPrimaryKey();
                Example example = conditionDelete.getExample();
                if (primaryKey != null) {
                    totalCount += baseMapper.deleteById((Serializable) primaryKey);

                } else if (example != null) {
                    totalCount += baseMapper.delete(buildUpdateWrapper(example));
                }
            }
        }
        return totalCount;
    }

    private UpdateWrapper<Object> buildUpdateWrapper(Object primaryKey) {
        UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(entityStoreInfo.getIdColumn(), primaryKey);
        return updateWrapper;
    }

    private UpdateWrapper<Object> buildUpdateWrapper(Example example) {
        UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();
        AppenderContext.appendCriterion(updateWrapper, example);
        return updateWrapper;
    }

    private void buildUpdateWrapper(UpdateWrapper<Object> updateWrapper, Object persistentObj, Set<String> nullableProps) {
        Map<String, String> propAliasMappingWithoutPk = entityStoreInfo.getPropAliasMappingWithoutPk();
        propAliasMappingWithoutPk.forEach((prop, alias) -> {
            Object value = BeanUtil.getFieldValue(persistentObj, prop);
            if (value != null || nullableProps.contains(alias)) {
                updateWrapper.set(true, alias, value);
            }
        });
    }

}
