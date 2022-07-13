package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityCriterion;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class EntityExample {

    protected boolean emptyQuery = false;
    protected Object example;
    protected Set<String> selectColumns;
    protected List<EntityCriterion> entityCriteria = new ArrayList<>();
    protected String[] orderBy;
    protected String sort;

    public EntityExample(Object example) {
        this.example = example;
    }

    public boolean isDirtyQuery() {
        return entityCriteria.size() > 0;
    }

    public boolean isAllQuery() {
        return !emptyQuery && !isDirtyQuery();
    }

    public void addCriterion(EntityCriterion entityCriterion) {
        entityCriteria.add(entityCriterion);
    }

    public EntityExample selectColumns() {
        return this;
    }

    public EntityExample appendCriteria() {
        if (example != null) {
            for (EntityCriterion entityCriterion : entityCriteria) {
                entityCriterion.appendTo(this);
            }
        }
        return this;
    }

    public EntityExample orderBy() {
        return this;
    }

    public Object buildExample() {
        return selectColumns().appendCriteria().orderBy().getExample();
    }

}
