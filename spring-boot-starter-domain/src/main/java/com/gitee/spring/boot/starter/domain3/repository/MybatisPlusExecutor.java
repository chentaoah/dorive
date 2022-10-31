package com.gitee.spring.boot.starter.domain3.repository;

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
import com.gitee.spring.boot.starter.domain.api.CriterionAppender;
import com.gitee.spring.domain.core3.api.EntityFactory;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.entity.executor.*;
import com.gitee.spring.domain.core3.entity.operation.*;
import com.gitee.spring.domain.core3.impl.executor.AbstractExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

import static com.gitee.spring.boot.starter.domain.appender.AppenderContext.OPERATOR_CRITERION_APPENDER_MAP;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MybatisPlusExecutor extends AbstractExecutor {

    private ElementDefinition elementDefinition;
    private EntityDefinition entityDefinition;
    private BaseMapper<Object> baseMapper;
    private Class<Object> pojoClass;
    private String[] orderBy;
    private String sort;
    private EntityFactory entityFactory;

    @Override
    public Result executeQuery(BoundedContext boundedContext, Query query) {
        if (query.getPrimaryKey() != null) {
            QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", query.getPrimaryKey());
            List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
            Object entity = !resultMaps.isEmpty() ? entityFactory.reconstitute(boundedContext, resultMaps.get(0)) : null;
            return new Result(entity);

        } else if (query.withoutPage()) {
            Example example = query.getExample();
            if (example instanceof UnionExample) {
                QueryWrapper<Object> queryWrapper = buildQueryWrapper((UnionExample) example);
                List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
                if (boundedContext.containsKey("#fishhook")) {
                    Fishhook fishhook = (Fishhook) boundedContext.get("#fishhook");
                    fishhook.setSource(resultMaps);
                    boundedContext.remove("#fishhook");
                }
                List<Object> entities = new ArrayList<>(resultMaps.size());
                Set<Object> existIds = new HashSet<>();
                for (Map<String, Object> resultMap : resultMaps) {
                    Object id = resultMap.get("id");
                    if (existIds.add(id)) {
                        Object entity = entityFactory.reconstitute(boundedContext, resultMap);
                        entities.add(entity);
                    }
                }
                return new Result(entities);

            } else {
                QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);
                List<Map<String, Object>> resultMaps = baseMapper.selectMaps(queryWrapper);
                List<Object> entities = new ArrayList<>(resultMaps.size());
                for (Map<String, Object> resultMap : resultMaps) {
                    Object entity = entityFactory.reconstitute(boundedContext, resultMap);
                    entities.add(entity);
                }
                return new Result(entities);
            }

        } else {
            Example example = query.getExample();
            com.gitee.spring.domain.core3.entity.executor.Page<Object> page = example.getPage();

            Page<Map<String, Object>> dataPage = new Page<>(page.getCurrent(), page.getSize());
            QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);
            dataPage = baseMapper.selectMapsPage(dataPage, queryWrapper);

            page.setTotal(dataPage.getTotal());

            List<Map<String, Object>> resultMaps = dataPage.getRecords();
            List<Object> entities = new ArrayList<>(resultMaps.size());
            for (Map<String, Object> resultMap : resultMaps) {
                Object entity = entityFactory.reconstitute(boundedContext, resultMap);
                entities.add(entity);
            }
            page.setRecords(entities);

            return new Result(page);
        }
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
        String[] orderBy = example.getOrderBy() != null ? example.getOrderBy() : this.orderBy;
        String sort = example.getSort() != null ? example.getSort() : this.sort;
        if (orderBy != null && sort != null) {
            if ("asc".equals(sort)) {
                queryWrapper.orderByAsc(orderBy);
            } else if ("desc".equals(sort)) {
                queryWrapper.orderByDesc(orderBy);
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
            lastSql.append(example.getPage().toString()).append(" ");
        }
        for (int index = 1; index < examples.size(); index++) {
            Example nextExample = examples.get(index);
            QueryWrapper<Object> nextQueryWrapper = buildQueryWrapper(nextExample);

            String sqlSelect = nextQueryWrapper.getSqlSelect();
            String tableName = TableInfoHelper.getTableInfo(pojoClass).getTableName();
            String criteria = buildCriteria(nextExample);

            String sql;
            if (nextExample.getPage() == null) {
                sql = String.format("UNION ALL (SELECT %s FROM %s WHERE %s)", sqlSelect, tableName, criteria);
            } else {
                String limit = nextExample.getPage().toString();
                sql = String.format("UNION ALL (SELECT %s FROM %s WHERE %s %s)", sqlSelect, tableName, criteria, limit);
            }
            lastSql.append(sql);
        }
        if (lastSql.length() > 0) {
            queryWrapper.last(lastSql.toString());
        }
        return queryWrapper;
    }

    private String buildCriteria(Example example) {
        return StrUtil.join(" AND ", example.getCriteria());
    }

    @Override
    @SuppressWarnings("unchecked")
    public int execute(BoundedContext boundedContext, Operation operation) {
        Object entity = operation.getEntity();
        Object persistentObject = entity != null ? entityFactory.deconstruct(boundedContext, entity) : null;
        if (operation instanceof Insert) {
            int count = baseMapper.insert(persistentObject);
            Object primaryKey = BeanUtil.getFieldValue(persistentObject, "id");
            BeanUtil.setFieldValue(entity, "id", primaryKey);
            return count;

        } else if (operation instanceof Update) {
            Update update = (Update) operation;
            Object primaryKey = update.getPrimaryKey();
            Example example = update.getExample();

            String forceInsertKey = entityDefinition.getForceInsertKey();
            if (StringUtils.isNotBlank(forceInsertKey) && boundedContext.containsKey(forceInsertKey)) {
                return baseMapper.insert(persistentObject);
            }

            String nullableKey = entityDefinition.getNullableKey();
            if (StringUtils.isNotBlank(nullableKey)) {
                Set<String> nullableProperties = (Set<String>) boundedContext.get(nullableKey);
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
