package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.utils.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class EntityExample {

    protected boolean emptyQuery = false;
    protected Set<String> selectColumns;
    protected List<EntityCriterion> entityCriteria = new ArrayList<>();
    protected String[] orderBy;
    protected String sort;

    public boolean isDirtyQuery() {
        return entityCriteria.size() > 0;
    }

    public boolean isAllQuery() {
        return !emptyQuery && !isDirtyQuery();
    }

    public void addCriterion(EntityCriterion entityCriterion) {
        entityCriteria.add(entityCriterion);
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
