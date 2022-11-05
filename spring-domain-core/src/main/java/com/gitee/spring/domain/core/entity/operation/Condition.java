package com.gitee.spring.domain.core.entity.operation;

import com.gitee.spring.domain.core.entity.executor.Example;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Condition extends Operation {

    protected Object primaryKey;
    protected Example example;

    public Condition(int type, Object entity) {
        super(type, entity);
    }

}
