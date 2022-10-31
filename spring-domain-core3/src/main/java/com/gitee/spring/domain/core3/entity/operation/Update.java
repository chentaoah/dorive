package com.gitee.spring.domain.core3.entity.operation;

import com.gitee.spring.domain.core3.entity.executor.Example;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Update extends Operation {

    private Object primaryKey;
    private Example example;

    public Update(int type, Object entity) {
        super(type, entity);
    }

    public Update(Object entity, Object primaryKey) {
        super(entity);
        this.primaryKey = primaryKey;
    }

    public Update(Object entity, Example example) {
        super(entity);
        this.example = example;
    }

}
