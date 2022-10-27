package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.domain.api.CriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.EQCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.GECriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.GTCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.InCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.IsNotNullCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.IsNullCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.LECriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.LTCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.LikeCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.NECriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.NotInCriterionAppender;
import com.gitee.spring.boot.starter.domain.builder.NotLikeCriterionAppender;
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

    public static Map<String, CriterionAppender> operatorCriterionAppenderMap = new ConcurrentHashMap<>();
    protected EntityDefinition entityDefinition;

    static {
        operatorCriterionAppenderMap.put(Operator.EQ, new EQCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.NE, new NECriterionAppender());
        operatorCriterionAppenderMap.put(Operator.IN, new InCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.NOT_IN, new NotInCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.IS_NULL, new IsNullCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.IS_NOT_NULL, new IsNotNullCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.LIKE, new LikeCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.NOT_LIKE, new NotLikeCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.GT, new GTCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.GE, new GECriterionAppender());
        operatorCriterionAppenderMap.put(Operator.LT, new LTCriterionAppender());
        operatorCriterionAppenderMap.put(Operator.LE, new LECriterionAppender());
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
            CriterionAppender criterionAppender = operatorCriterionAppenderMap.get(operator);
            criterionAppender.appendCriterion(queryWrapper, StrUtil.toUnderlineCase(fieldName), fieldValue);
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
