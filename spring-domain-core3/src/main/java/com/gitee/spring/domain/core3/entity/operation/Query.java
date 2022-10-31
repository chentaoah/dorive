package com.gitee.spring.domain.core3.entity.operation;

import com.gitee.spring.domain.core3.entity.executor.Example;
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

    public boolean withoutPage() {
        return example.getPage() == null;
    }

}
