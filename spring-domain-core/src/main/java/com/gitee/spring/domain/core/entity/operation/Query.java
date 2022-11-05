package com.gitee.spring.domain.core.entity.operation;

public class Query extends Condition {

    public Query(int type, Object entity) {
        super(type, entity);
    }

    public boolean withoutPage() {
        return example.getPage() == null;
    }

}
