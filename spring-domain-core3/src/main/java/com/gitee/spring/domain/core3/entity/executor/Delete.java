package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Delete extends Operation {

    private Object primaryKey;
    private Example example;

    public Delete(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Delete(Example example) {
        this.example = example;
    }

}
