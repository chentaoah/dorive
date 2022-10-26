package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Query extends Operation {

    private Object primaryKey;
    private Example example;

    public Query(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Query(Example example) {
        this.example = example;
    }

    public Query buildExampleByPK() {
        example = new Example().eq("id", primaryKey);
        return this;
    }

    public boolean startPage() {
        return example.getPageNum() != null && example.getPageSize() != null;
    }

}
