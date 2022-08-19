package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.domain.api.ExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.EQExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.GEExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.GTExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.InExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.IsNotNullExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.IsNullExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.LEExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.LTExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.LikeExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.NEExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.NotInExampleBuilder;
import com.gitee.spring.boot.starter.domain.builder.NotLikeExampleBuilder;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityCriterion;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MybatisPlusEntityMapper implements EntityMapper {

    public static Map<String, ExampleBuilder> operatorExampleBuilderMap = new ConcurrentHashMap<>();
    protected EntityDefinition entityDefinition;

    static {
        operatorExampleBuilderMap.put(Operator.EQ, new EQExampleBuilder());
        operatorExampleBuilderMap.put(Operator.NE, new NEExampleBuilder());
        operatorExampleBuilderMap.put(Operator.IN, new InExampleBuilder());
        operatorExampleBuilderMap.put(Operator.NOT_IN, new NotInExampleBuilder());
        operatorExampleBuilderMap.put(Operator.IS_NULL, new IsNullExampleBuilder());
        operatorExampleBuilderMap.put(Operator.IS_NOT_NULL, new IsNotNullExampleBuilder());
        operatorExampleBuilderMap.put(Operator.LIKE, new LikeExampleBuilder());
        operatorExampleBuilderMap.put(Operator.NOT_LIKE, new NotLikeExampleBuilder());
        operatorExampleBuilderMap.put(Operator.GT, new GTExampleBuilder());
        operatorExampleBuilderMap.put(Operator.GE, new GEExampleBuilder());
        operatorExampleBuilderMap.put(Operator.LT, new LTExampleBuilder());
        operatorExampleBuilderMap.put(Operator.LE, new LEExampleBuilder());
    }

    public MybatisPlusEntityMapper(EntityDefinition entityDefinition) {
        this.entityDefinition = entityDefinition;
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
    public Object buildExample(BoundedContext boundedContext, EntityExample entityExample) {
        QueryWrapper<?> queryWrapper = new QueryWrapper<>();
        String[] selectColumns = entityExample.getSelectColumns();
        if (selectColumns != null) {
            queryWrapper.select(selectColumns);
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
