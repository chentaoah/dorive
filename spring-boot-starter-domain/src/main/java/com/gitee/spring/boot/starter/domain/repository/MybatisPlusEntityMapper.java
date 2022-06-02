package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.AbstractEntityCriterion;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;

import java.util.Collection;
import java.util.List;

public class MybatisPlusEntityMapper implements EntityMapper {

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
            public Object buildExample() {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) example;
                if (columns != null) {
                    queryWrapper.select(columns.toArray(new String[0]));
                }
                if (orderBy != null && sort != null) {
                    if ("asc".equals(sort)) {
                        queryWrapper.orderByAsc(orderBy);

                    } else if ("desc".equals(sort)) {
                        queryWrapper.orderByDesc(orderBy);
                    }
                }
                return super.buildExample();
            }
        };
        entityExample.setOrderBy(entityDefinition.getOrderBy());
        entityExample.setSort(entityDefinition.getSort());
        return entityExample;
    }

    @Override
    public EntityCriterion newEqualCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, fieldValue) {
            @Override
            public void appendTo(EntityExample entityExample) {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) entityExample.getExample();
                String fieldName = StrUtil.toUnderlineCase(this.fieldName);
                if (fieldValue instanceof Collection) {
                    queryWrapper.in(fieldName, (Collection<?>) fieldValue);
                } else {
                    queryWrapper.eq(fieldName, fieldValue);
                }
            }
        };
    }

    @Override
    public EntityCriterion newGreaterThanCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, fieldValue) {
            @Override
            public void appendTo(EntityExample entityExample) {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) entityExample.getExample();
                String fieldName = StrUtil.toUnderlineCase(this.fieldName);
                queryWrapper.gt(fieldName, fieldValue);
            }
        };
    }

    @Override
    public EntityCriterion newGreaterThanOrEqualCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, fieldValue) {
            @Override
            public void appendTo(EntityExample entityExample) {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) entityExample.getExample();
                String fieldName = StrUtil.toUnderlineCase(this.fieldName);
                queryWrapper.ge(fieldName, fieldValue);
            }
        };
    }

    @Override
    public EntityCriterion newLessThanCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, fieldValue) {
            @Override
            public void appendTo(EntityExample entityExample) {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) entityExample.getExample();
                String fieldName = StrUtil.toUnderlineCase(this.fieldName);
                queryWrapper.lt(fieldName, fieldValue);
            }
        };
    }

    @Override
    public EntityCriterion newLessThanOrEqualCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, fieldValue) {
            @Override
            public void appendTo(EntityExample entityExample) {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) entityExample.getExample();
                String fieldName = StrUtil.toUnderlineCase(this.fieldName);
                queryWrapper.le(fieldName, fieldValue);
            }
        };
    }

}
