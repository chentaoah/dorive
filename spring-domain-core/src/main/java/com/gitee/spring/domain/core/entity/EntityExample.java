package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityCriterion;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EntityExample {

    protected boolean emptyQuery = false;
    protected Object example;
    protected List<String> columns;
    protected List<EntityCriterion> entityCriteria = new ArrayList<>();

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

    public Object buildExample() {
        if (example != null) {
            for (EntityCriterion entityCriterion : entityCriteria) {
                entityCriterion.appendTo(this);
            }
        }
        return example;
    }

    public Object getBuiltExample() {
        return buildExample();
    }

}
