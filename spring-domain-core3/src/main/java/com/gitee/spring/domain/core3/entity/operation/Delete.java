package com.gitee.spring.domain.core3.entity.operation;

import com.gitee.spring.domain.core3.entity.executor.Example;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Delete extends Operation {

    private Object primaryKey;
    private Example example;

    public Delete(int type, Object entity) {
        super(type, entity);
    }

    public Delete(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Delete(Example example) {
        this.example = example;
    }

}
