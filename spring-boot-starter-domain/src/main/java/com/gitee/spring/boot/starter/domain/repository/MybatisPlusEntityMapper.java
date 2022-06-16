package com.gitee.spring.boot.starter.domain.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.domain.api.EntityCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.*;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MybatisPlusEntityMapper implements EntityMapper {

    public static Map<String, EntityCriterionBuilder> operatorEntityCriterionBuilderMap = new ConcurrentHashMap<>();

    static {
        operatorEntityCriterionBuilderMap.put(Operator.EQ, new EQEntityCriterionBuilder());
        operatorEntityCriterionBuilderMap.put(Operator.GT, new GTEntityCriterionBuilder());
        operatorEntityCriterionBuilderMap.put(Operator.GE, new GEEntityCriterionBuilder());
        operatorEntityCriterionBuilderMap.put(Operator.LT, new LTEntityCriterionBuilder());
        operatorEntityCriterionBuilderMap.put(Operator.LE, new LEEntityCriterionBuilder());
    }

    @Override
    public Object newPage(Integer pageNum, Integer pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    @Override
    public List<?> getDataFromPage(Object dataPage) {
        return ((IPage<?>) dataPage).getRecords();
    }

    @Override
    public Object newPageOfEntities(Object dataPage, List<Object> entities) {
        IPage<?> page = (IPage<?>) dataPage;
        IPage<Object> newPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        newPage.setRecords(entities);
        return newPage;
    }

    @Override
    public EntityExample newExample(EntityDefinition entityDefinition, BoundedContext boundedContext) {
        EntityExample entityExample = new EntityExample(new QueryWrapper<>()) {
            @Override
            public EntityExample selectColumns() {
                if (columns != null) {
                    QueryWrapper<?> queryWrapper = (QueryWrapper<?>) example;
                    queryWrapper.select(columns.toArray(new String[0]));
                }
                return this;
            }

            @Override
            public EntityExample orderBy() {
                if (orderBy != null && sort != null) {
                    QueryWrapper<?> queryWrapper = (QueryWrapper<?>) example;
                    if ("asc".equals(sort)) {
                        queryWrapper.orderByAsc(orderBy);

                    } else if ("desc".equals(sort)) {
                        queryWrapper.orderByDesc(orderBy);
                    }
                }
                return this;
            }
        };
        entityExample.setOrderBy(entityDefinition.getOrderBy());
        entityExample.setSort(entityDefinition.getSort());
        return entityExample;
    }

    @Override
    public EntityCriterion newCriterion(String fieldName, String operator, Object fieldValue) {
        EntityCriterionBuilder entityCriterionBuilder = operatorEntityCriterionBuilderMap.get(operator);
        return entityCriterionBuilder.newCriterion(fieldName, fieldValue);
    }

}
