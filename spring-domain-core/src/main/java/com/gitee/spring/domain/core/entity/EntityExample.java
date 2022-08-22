package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.utils.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class EntityExample {

    protected boolean emptyQuery = false;
    protected String[] selectColumns;
    protected List<EntityCriterion> entityCriteria = new ArrayList<>();
    protected String[] orderBy;
    protected String sort;

    public boolean isDirtyQuery() {
        return entityCriteria.size() > 0;
    }

    public boolean isAllQuery() {
        return !emptyQuery && !isDirtyQuery();
    }

    public void setSelectColumns(String... columns) {
        selectColumns = StringUtils.toUnderlineCase(columns);
    }

    public void addCriterion(EntityCriterion entityCriterion) {
        entityCriteria.add(entityCriterion);
    }

    public EntityExample eq(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.EQ, fieldValue));
        return this;
    }

    public EntityExample ne(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.NE, fieldValue));
        return this;
    }

    public EntityExample in(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.IN, fieldValue));
        return this;
    }

    public EntityExample notIn(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.NOT_IN, fieldValue));
        return this;
    }

    public EntityExample isNull(String fieldName) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.IS_NULL, null));
        return this;
    }

    public EntityExample isNotNull(String fieldName) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.IS_NOT_NULL, null));
        return this;
    }

    public EntityExample like(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.LIKE, fieldValue));
        return this;
    }

    public EntityExample notLike(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.NOT_LIKE, fieldValue));
        return this;
    }

    public EntityExample gt(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.GT, fieldValue));
        return this;
    }

    public EntityExample ge(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.GE, fieldValue));
        return this;
    }

    public EntityExample lt(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.LT, fieldValue));
        return this;
    }

    public EntityExample le(String fieldName, Object fieldValue) {
        entityCriteria.add(new EntityCriterion(fieldName, Operator.LE, fieldValue));
        return this;
    }

    public void orderByAsc(String... columns) {
        orderBy = StringUtils.toUnderlineCase(columns);
        sort = "asc";
    }

    public void orderByDesc(String... columns) {
        orderBy = StringUtils.toUnderlineCase(columns);
        sort = "desc";
    }

}
