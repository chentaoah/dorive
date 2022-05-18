package com.gitee.spring.domain.core.entity;

import com.gitee.spring.domain.core.api.EntityCriterion;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EntityExample {

    protected boolean emptyQuery = false;
    protected boolean dirtyQuery = false;
    protected List<String> columns;
    protected Object example;
    protected List<EntityCriterion> entityCriteria = new ArrayList<>();

    public EntityExample(Object example) {
        this.example = example;
    }

    public boolean isAllQuery() {
        return !emptyQuery && !dirtyQuery;
    }

    public void addCriterion(EntityCriterion entityCriterion) {
        dirtyQuery = true;
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

}
