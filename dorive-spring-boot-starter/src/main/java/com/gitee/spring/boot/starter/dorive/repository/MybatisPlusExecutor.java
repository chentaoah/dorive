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
package com.gitee.spring.boot.starter.dorive.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.dorive.api.CriterionAppender;
import com.gitee.spring.boot.starter.dorive.entity.Metadata;
import com.gitee.spring.boot.starter.dorive.impl.EntityIndexResult;
import com.gitee.dorive.core.api.EntityFactory;
import com.gitee.dorive.core.api.MetadataHolder;
import com.gitee.dorive.core.api.PropertyProxy;
import com.gitee.dorive.core.api.constant.Order;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.Command;
import com.gitee.dorive.core.entity.EntityElement;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.entity.executor.Result;
import com.gitee.dorive.core.entity.executor.UnionExample;
import com.gitee.dorive.core.entity.operation.Delete;
import com.gitee.dorive.core.entity.operation.Insert;
import com.gitee.dorive.core.entity.operation.Operation;
import com.gitee.dorive.core.entity.operation.Query;
import com.gitee.dorive.core.entity.operation.Update;
import com.gitee.dorive.core.impl.executor.AbstractExecutor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gitee.spring.boot.starter.dorive.impl.AppenderContext.OPERATOR_CRITERION_APPENDER_MAP;

@Getter
@Setter
@ToString
public class MybatisPlusExecutor extends AbstractExecutor implements MetadataHolder {

    private EntityDefinition entityDefinition;
    private BaseMapper<Object> baseMapper;
    private Class<Object> pojoClass;
    private EntityFactory entityFactory;

    public MybatisPlusExecutor(EntityElement entityElement,
                               EntityDefinition entityDefinition,
                               BaseMapper<Object> baseMapper,
                               Class<Object> pojoClass,
                               EntityFactory entityFactory) {
        super(entityElement);
        this.entityDefinition = entityDefinition;
        this.baseMapper = baseMapper;
        this.pojoClass = pojoClass;
        this.entityFactory = entityFactory;
    }

    @Override
    public Object getMetadata() {
        return new Metadata(pojoClass);
    }

    @Override
    public Result<Object> executeQuery(BoundedContext boundedContext, Query query) {
        if (query.getPrimaryKey() != null) {
            QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", query.getPrimaryKey());
            List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
            List<Object> entities = reconstitute(boundedContext, resultMaps);
            return new Result<>(entities);

        } else if (query.getExample() != null) {
            Example example = query.getExample();
            if (query.withoutPage()) {
                if (example instanceof UnionExample) {
                    UnionExample unionExample = (UnionExample) example;
                    QueryWrapper<Object> queryWrapper = buildQueryWrapper(unionExample);
                    List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
                    int resultSize = resultMaps.size();
                    Set<Object> existIds = new HashSet<>(resultSize * 4 / 3 + 1);
                    List<Object> entities = new ArrayList<>(resultSize);
                    for (Map<String, Object> resultMap : resultMaps) {
                        Object id = resultMap.get("id");
                        if (existIds.add(id)) {
                            Object entity = entityFactory.reconstitute(boundedContext, resultMap);
                            entities.add(entity);
                        }
                    }
                    return new EntityIndexResult(unionExample, resultMaps, entities, entityElement.getPrimaryKeyProxy());

                } else {
                    QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);
                    List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
                    List<Object> entities = reconstitute(boundedContext, resultMaps);
                    return new Result<>(entities);
                }

            } else {
                com.gitee.dorive.core.entity.executor.Page<Object> page = example.getPage();
                Page<Map<String, Object>> dataPage = new Page<>(page.getCurrent(), page.getSize());
                QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);

                dataPage = baseMapper.selectMapsPage(dataPage, queryWrapper);
                page.setTotal(dataPage.getTotal());

                List<Map<String, Object>> resultMaps = dataPage.getRecords();
                List<Object> entities = reconstitute(boundedContext, resultMaps);
                page.setRecords(entities);

                return new Result<>(page);
            }

        } else {
            throw new RuntimeException("Unsupported query method!");
        }
    }

    private List<Object> reconstitute(BoundedContext boundedContext, List<Map<String, Object>> resultMaps) {
        List<Object> entities = new ArrayList<>(resultMaps.size());
        for (Map<String, Object> resultMap : resultMaps) {
            Object entity = entityFactory.reconstitute(boundedContext, resultMap);
            entities.add(entity);
        }
        return entities;
    }

    private QueryWrapper<Object> buildQueryWrapper(Example example) {
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();

        String[] selectColumns = example.getSelectColumns();
        if (selectColumns != null) {
            String sqlSelect = queryWrapper.select(pojoClass, i -> true).getSqlSelect();
            sqlSelect = sqlSelect + StringPool.COMMA + queryWrapper.select(selectColumns).getSqlSelect();
            queryWrapper.select(sqlSelect);
        }

        for (Criterion criterion : example.getCriteria()) {
            CriterionAppender criterionAppender = OPERATOR_CRITERION_APPENDER_MAP.get(criterion.getOperator());
            String property = StrUtil.toUnderlineCase(criterion.getProperty());
            criterionAppender.appendCriterion(queryWrapper, property, criterion.getValue());
        }

        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            String order = orderBy.getOrder();
            if (Order.ASC.equals(order)) {
                queryWrapper.orderByAsc(orderBy.getColumns());

            } else if (Order.DESC.equals(order)) {
                queryWrapper.orderByDesc(orderBy.getColumns());
            }
        }

        return queryWrapper;
    }

    private QueryWrapper<Object> buildQueryWrapper(UnionExample unionExample) {
        List<Example> examples = unionExample.getExamples();
        Assert.notEmpty(examples, "The examples cannot be empty!");

        Example example = examples.get(0);
        QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);

        StringBuilder lastSql = new StringBuilder();
        if (example.getPage() != null) {
            lastSql.append(example.getPage()).append(" ");
        }

        for (int index = 1; index < examples.size(); index++) {
            Example nextExample = examples.get(index);
            QueryWrapper<Object> nextQueryWrapper = buildQueryWrapper(nextExample);

            String sqlSelect = nextQueryWrapper.getSqlSelect();
            String tableName = TableInfoHelper.getTableInfo(pojoClass).getTableName();
            String criteria = nextExample.buildCriteria();

            String sql = "";
            if (nextExample.getOrderBy() == null && nextExample.getPage() == null) {
                sql = String.format("UNION ALL (SELECT %s FROM %s WHERE %s) ", sqlSelect, tableName, criteria);

            } else if (nextExample.getOrderBy() != null && nextExample.getPage() != null) {
                sql = String.format("UNION ALL (SELECT %s FROM %s WHERE %s %s %s) ", sqlSelect, tableName, criteria, nextExample.getOrderBy(), nextExample.getPage());

            } else if (nextExample.getOrderBy() != null) {
                sql = String.format("UNION ALL (SELECT %s FROM %s WHERE %s %s) ", sqlSelect, tableName, criteria, nextExample.getOrderBy());

            } else if (nextExample.getPage() != null) {
                sql = String.format("UNION ALL (SELECT %s FROM %s WHERE %s %s) ", sqlSelect, tableName, criteria, nextExample.getPage());
            }
            lastSql.append(sql);
        }

        if (lastSql.length() > 0) {
            queryWrapper.last(lastSql.toString());
        }
        return queryWrapper;
    }

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        Object entity = operation.getEntity();
        Object persistentObject = entity != null ? entityFactory.deconstruct(boundedContext, entity) : null;

        if (operation instanceof Insert) {
            int count = baseMapper.insert(persistentObject);
            Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
            PropertyProxy primaryKeyProxy = entityElement.getPrimaryKeyProxy();
            primaryKeyProxy.setValue(entity, primaryKey);
            return count;

        } else if (operation instanceof Update) {
            Update update = (Update) operation;
            Object primaryKey = update.getPrimaryKey();
            Example example = update.getExample();

            String commandKey = entityDefinition.getCommandKey();
            if (StringUtils.isNotBlank(commandKey) && boundedContext.containsKey(commandKey)) {
                Command command = (Command) boundedContext.get(commandKey);
                Set<String> nullableProperties = command.getNullableProperties();
                if (nullableProperties != null && !nullableProperties.isEmpty()) {
                    example = primaryKey != null ? new Example().eq("id", primaryKey) : example;
                    UpdateWrapper<Object> updateWrapper = buildUpdateWrapper(persistentObject, nullableProperties, example);
                    return baseMapper.update(null, updateWrapper);
                }
            }

            if (primaryKey != null) {
                return baseMapper.updateById(persistentObject);

            } else if (example != null) {
                return baseMapper.update(persistentObject, buildUpdateWrapper(example));
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
        for (Criterion criterion : example.getCriteria()) {
            CriterionAppender criterionAppender = OPERATOR_CRITERION_APPENDER_MAP.get(criterion.getOperator());
            String property = StrUtil.toUnderlineCase(criterion.getProperty());
            criterionAppender.appendCriterion(updateWrapper, property, criterion.getValue());
        }
        return updateWrapper;
    }

    private UpdateWrapper<Object> buildUpdateWrapper(Object persistentObject, Set<String> nullableProperties, Example example) {
        UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();
        List<TableFieldInfo> fieldList = TableInfoHelper.getTableInfo(pojoClass).getFieldList();
        for (TableFieldInfo tableFieldInfo : fieldList) {
            String property = tableFieldInfo.getProperty();
            Object value = BeanUtil.getFieldValue(persistentObject, property);
            if (value != null || nullableProperties.contains(property)) {
                updateWrapper.set(true, tableFieldInfo.getColumn(), value);
            }
        }
        for (Criterion criterion : example.getCriteria()) {
            CriterionAppender criterionAppender = OPERATOR_CRITERION_APPENDER_MAP.get(criterion.getOperator());
            String property = StrUtil.toUnderlineCase(criterion.getProperty());
            criterionAppender.appendCriterion(updateWrapper, property, criterion.getValue());
        }
        return updateWrapper;
    }

}
