package com.gitee.spring.domain.core3.entity.executor;

import cn.hutool.core.util.ArrayUtil;
import com.gitee.spring.domain.common.constant.Operator;
import com.gitee.spring.domain.common.util.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Example {

    private boolean emptyQuery = false;
    private String[] selectColumns;
    private List<Criterion> criteria = new ArrayList<>(4);
    private String[] orderBy;
    private String sort;
    private Page<Object> page;

    public boolean isDirtyQuery() {
        return criteria.size() > 0;
    }

    public boolean isQueryAll() {
        return !emptyQuery && !isDirtyQuery();
    }

    public void selectColumns(String... columns) {
        selectColumns = selectColumns == null ? columns : ArrayUtil.addAll(selectColumns, columns);
    }

    public void addCriterion(Criterion criterion) {
        criteria.add(criterion);
    }

    public Example eq(String property, Object value) {
        criteria.add(new Criterion(property, Operator.EQ, value));
        return this;
    }

    public Example ne(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NE, value));
        return this;
    }

    public Example in(String property, Object value) {
        criteria.add(new Criterion(property, Operator.IN, value));
        return this;
    }

    public Example notIn(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NOT_IN, value));
        return this;
    }

    public Example isNull(String property) {
        criteria.add(new Criterion(property, Operator.IS_NULL, null));
        return this;
    }

    public Example isNotNull(String property) {
        criteria.add(new Criterion(property, Operator.IS_NOT_NULL, null));
        return this;
    }

    public Example like(String property, Object value) {
        criteria.add(new Criterion(property, Operator.LIKE, value));
        return this;
    }

    public Example notLike(String property, Object value) {
        criteria.add(new Criterion(property, Operator.NOT_LIKE, value));
        return this;
    }

    public Example gt(String property, Object value) {
        criteria.add(new Criterion(property, Operator.GT, value));
        return this;
    }

    public Example ge(String property, Object value) {
        criteria.add(new Criterion(property, Operator.GE, value));
        return this;
    }

    public Example lt(String property, Object value) {
        criteria.add(new Criterion(property, Operator.LT, value));
        return this;
    }

    public Example le(String property, Object value) {
        criteria.add(new Criterion(property, Operator.LE, value));
        return this;
    }

    public Example orderByAsc(String... columns) {
        orderBy = StringUtils.toUnderlineCase(columns);
        sort = "asc";
        return this;
    }

    public Example orderByDesc(String... columns) {
        orderBy = StringUtils.toUnderlineCase(columns);
        sort = "desc";
        return this;
    }

    public Example startPage(long pageNum, long pageSize) {
        page = new Page<>(pageNum, pageSize);
        return this;
    }

    public Example startPage() {
        page = new Page<>();
        return this;
    }

}
