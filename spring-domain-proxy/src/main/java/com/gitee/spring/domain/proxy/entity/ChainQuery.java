package com.gitee.spring.domain.proxy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChainQuery {

    private List<Criterion> criteria = new ArrayList<>();

    public static ChainQuery create() {
        return new ChainQuery();
    }

    public ChainQuery and(Class<?> entityClass, Object example) {
        criteria.add(new Criterion(entityClass, example));
        return this;
    }

    public ChainQuery and(Class<?> entityClass) {
        criteria.add(new Criterion(entityClass, null));
        return this;
    }

    @Data
    @AllArgsConstructor
    public static class Criterion {
        private Class<?> entityClass;
        private Object example;
    }

}
