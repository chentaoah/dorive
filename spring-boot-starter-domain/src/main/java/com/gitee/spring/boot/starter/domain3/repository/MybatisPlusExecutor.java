package com.gitee.spring.boot.starter.domain3.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.boot.starter.domain.api.CriterionBuilder;
import com.gitee.spring.boot.starter.domain.repository.MybatisPlusEntityMapper;
import com.gitee.spring.domain.core3.api.EntityFactory;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.entity.executor.Criterion;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.core3.entity.executor.Operation;
import com.gitee.spring.domain.core3.entity.executor.Query;
import com.gitee.spring.domain.core3.entity.executor.Result;
import com.gitee.spring.domain.core3.impl.executor.AbstractExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MybatisPlusExecutor extends AbstractExecutor {

    private ElementDefinition elementDefinition;
    private EntityDefinition entityDefinition;
    private BaseMapper<Object> baseMapper;
    private Class<?> pojoClass;
    private String[] orderBy;
    private String sort;
    private EntityFactory entityFactory;

    @Override
    public Result executeQuery(BoundedContext boundedContext, Query query) {
        if (query.getPrimaryKey() != null) {
            QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("id", query.getPrimaryKey());
            List<Map<String, Object>> resultMap = baseMapper.selectMaps(queryWrapper);
            Object entity = null;
            if (!resultMap.isEmpty()) {
                entity = entityFactory.reconstitute(boundedContext, resultMap.get(0));
            }
            return new Result(entity);

        } else if (!query.startPage()) {
            QueryWrapper<Object> queryWrapper = buildQueryWrapper(query.getExample());
            List<Map<String, Object>> resultMap = baseMapper.selectMaps(queryWrapper);
            List<Object> entities = new ArrayList<>();
            for (Map<String, Object> record : resultMap) {
                Object entity = entityFactory.reconstitute(boundedContext, record);
                entities.add(entity);
            }
            return new Result(entities);

        } else {
            Example example = query.getExample();
            Page<Map<String, Object>> page = new Page<>(example.getPageNum(), example.getPageSize());
            QueryWrapper<Object> queryWrapper = buildQueryWrapper(example);
            page = baseMapper.selectMapsPage(page, queryWrapper);
            List<Object> entities = new ArrayList<>();
            for (Map<String, Object> record : page.getRecords()) {
                Object entity = entityFactory.reconstitute(boundedContext, record);
                entities.add(entity);
            }
            com.gitee.spring.domain.core3.entity.executor.Page<Object> newPage =
                    new com.gitee.spring.domain.core3.entity.executor.Page<>();
            newPage.setTotal(page.getTotal());
            newPage.setCurrent(page.getCurrent());
            newPage.setSize(page.getSize());
            newPage.setRecords(entities);
            return new Result(newPage);
        }
    }

    private QueryWrapper<Object> buildQueryWrapper(Example example) {
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        String[] selectColumns = example.getSelectColumns();
        if (selectColumns != null) {
            queryWrapper.select(selectColumns);
        }
        for (Criterion criterion : example.getCriteria()) {
            CriterionBuilder criterionBuilder = MybatisPlusEntityMapper.operatorCriterionBuilderMap.get(criterion.getOperator());
            criterionBuilder.appendCriterion(queryWrapper, StrUtil.toUnderlineCase(criterion.getProperty()), criterion.getValue());
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

    @Override
    public int execute(BoundedContext boundedContext, Operation operation) {
        return 0;
    }

}
