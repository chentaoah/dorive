package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.domain.api.ExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.*;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityCriterion;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MybatisPlusEntityMapper implements EntityMapper {

    public static Map<String, ExampleBuilder> operatorExampleBuilderMap = new ConcurrentHashMap<>();

    static {
        operatorExampleBuilderMap.put(Operator.EQ, new EQExampleBuilder());
        operatorExampleBuilderMap.put(Operator.GT, new GTExampleBuilder());
        operatorExampleBuilderMap.put(Operator.GE, new GEExampleBuilder());
        operatorExampleBuilderMap.put(Operator.LT, new LTExampleBuilder());
        operatorExampleBuilderMap.put(Operator.LE, new LEExampleBuilder());
        operatorExampleBuilderMap.put(Operator.LIKE, new LikeExampleBuilder());
    }

    @Override
    public Object newPage(Integer pageNum, Integer pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getDataFromPage(Object dataPage) {
        return ((IPage<Object>) dataPage).getRecords();
    }

    @Override
    public Object newPageOfEntities(Object dataPage, List<Object> entities) {
        IPage<?> page = (IPage<?>) dataPage;
        IPage<Object> newPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        newPage.setRecords(entities);
        return newPage;
    }

    @Override
    public Object buildExample(BoundedContext boundedContext, EntityDefinition entityDefinition, EntityExample entityExample) {
        QueryWrapper<?> queryWrapper = new QueryWrapper<>();
        Set<String> selectColumns = entityExample.getSelectColumns();
        if (selectColumns != null) {
            queryWrapper.select(selectColumns.toArray(new String[0]));
        }
        for (EntityCriterion entityCriterion : entityExample.getEntityCriteria()) {
            String fieldName = entityCriterion.getFieldName();
            String operator = entityCriterion.getOperator();
            Object fieldValue = entityCriterion.getFieldValue();
            ExampleBuilder exampleBuilder = operatorExampleBuilderMap.get(operator);
            exampleBuilder.appendCriterion(queryWrapper, StrUtil.toUnderlineCase(fieldName), fieldValue);
        }
        String[] orderBy = entityExample.getOrderBy() != null ? entityExample.getOrderBy() : entityDefinition.getOrderBy();
        String sort = entityExample.getSort() != null ? entityExample.getSort() : entityDefinition.getSort();
        if (orderBy != null && sort != null) {
            if ("asc".equals(sort)) {
                queryWrapper.orderByAsc(orderBy);
            } else if ("desc".equals(sort)) {
                queryWrapper.orderByDesc(orderBy);
            }
        }
        return queryWrapper;
    }

}
