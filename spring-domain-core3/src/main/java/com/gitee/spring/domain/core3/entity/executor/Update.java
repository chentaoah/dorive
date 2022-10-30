package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Update extends Operation {

    private Object entity;
    private Object primaryKey;
    private Example example;

    public Update(Object entity, Object primaryKey) {
        this.entity = entity;
        this.primaryKey = primaryKey;
    }

    public Update(Object entity, Example example) {
        this.entity = entity;
        this.example = example;
    }

}
