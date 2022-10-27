package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.domain.api.CriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.EQCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.GECriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.GTCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.InCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.IsNotNullCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.IsNullCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.LECriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.LTCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.LikeCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.NECriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.NotInCriterionBuilder;
import com.gitee.spring.boot.starter.domain.builder.NotLikeCriterionBuilder;
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

    public static Map<String, CriterionBuilder> operatorCriterionBuilderMap = new ConcurrentHashMap<>();
    protected EntityDefinition entityDefinition;

    static {
        operatorCriterionBuilderMap.put(Operator.EQ, new EQCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.NE, new NECriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.IN, new InCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.NOT_IN, new NotInCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.IS_NULL, new IsNullCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.IS_NOT_NULL, new IsNotNullCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.LIKE, new LikeCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.NOT_LIKE, new NotLikeCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.GT, new GTCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.GE, new GECriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.LT, new LTCriterionBuilder());
        operatorCriterionBuilderMap.put(Operator.LE, new LECriterionBuilder());
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
            CriterionBuilder criterionBuilder = operatorCriterionBuilderMap.get(operator);
            criterionBuilder.appendCriterion(queryWrapper, StrUtil.toUnderlineCase(fieldName), fieldValue);
        }
        String[] orderBy;
        if (entityExample.getOrderBy() != null) {
            orderBy = entityExample.getOrderBy();
        } else {
            orderBy = entityDefinition.getOrderBy();
        }
        String sort;
        if (entityExample.getSort() != null) {
            sort = entityExample.getSort();
        } else {
            sort = entityDefinition.getSort();
        }
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
